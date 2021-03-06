/*
 * @cond LICENSE
 * ######################################################################################
 * # LGPL License                                                                       #
 * #                                                                                    #
 * # This file is part of the LightJason AgentSpeak(L++)                                #
 * # Copyright (c) 2015-16, LightJason (info@lightjason.org)                            #
 * # This program is free software: you can redistribute it and/or modify               #
 * # it under the terms of the GNU Lesser General Public License as                     #
 * # published by the Free Software Foundation, either version 3 of the                 #
 * # License, or (at your option) any later version.                                    #
 * #                                                                                    #
 * # This program is distributed in the hope that it will be useful,                    #
 * # but WITHOUT ANY WARRANTY; without even the implied warranty of                     #
 * # MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                      #
 * # GNU Lesser General Public License for more details.                                #
 * #                                                                                    #
 * # You should have received a copy of the GNU Lesser General Public License           #
 * # along with this program. If not, see http://www.gnu.org/licenses/                  #
 * ######################################################################################
 * @endcond
 */

package org.lightjason.agentspeak.common;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.StringUtils;
import org.lightjason.agentspeak.error.CIllegalArgumentException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


/**
 * class to create a path structure
 */
public final class CPath implements IPath
{
    public static final String DEFAULTSEPERATOR = "/";
    /**
     * empty path
     **/
    public static final IPath EMPTY = new CPath();
    /**
     * list with path parts *
     */
    private final List<String> m_path;
    /**
     * separator of the path elements *
     */
    private String m_separator = DEFAULTSEPERATOR;

    /**
     * copy-ctor with arguments
     *
     * @param p_path path object
     * @param p_varargs string arguments
     */
    public CPath( final IPath p_path, final String... p_varargs )
    {
        this( p_path );
        Arrays.stream( p_varargs ).forEach( m_path::add );
        this.normalize();
    }

    /**
     * copy-ctor
     *
     * @param p_path path object
     */
    public CPath( final IPath p_path )
    {
        m_path = p_path.stream().collect( CPath.collectorfactory() );
        m_separator = p_path.getSeparator();
    }

    /**
     * ctor
     *
     * @param p_varargs path component
     */
    public CPath( final String... p_varargs )
    {
        if ( ( p_varargs == null ) || ( p_varargs.length == 0 ) )
            m_path = CPath.listfactory();
        else
        {
            m_path = Arrays.stream( StringUtils.join( p_varargs, m_separator ).split( m_separator ) )
                           .map( String::trim )
                           .filter( i -> !i.isEmpty() )
                           .collect( CPath.collectorfactory() );
            if ( m_path.size() == 0 )
                throw new CIllegalArgumentException( CCommon.languagestring( this, "pathempty" ) );
        }
        this.normalize();
    }

    /**
     * ctor
     *
     * @param p_stream string collection
     */
    public CPath( final Stream<String> p_stream )
    {
        m_path = p_stream.collect( CPath.collectorfactory() );
        this.normalize();
    }

    /**
     * private ctor for empty path
     */
    private CPath()
    {
        m_path = Collections.emptyList();
    }

    /**
     * creates a path object from different items
     *
     * @param p_varargs list of strings
     * @return path object
     */
    public static IPath createPath( final String... p_varargs )
    {
        if ( ( p_varargs == null ) || ( p_varargs.length < 1 ) )
            throw new CIllegalArgumentException( CCommon.languagestring( CPath.class, "createpath" ) );

        return new CPath( p_varargs );
    }

    /**
     * creates a path object by splitting a string
     *
     * @param p_varargs list of string (first element is the seperator)
     * @return path object
     */
    public static IPath createPathWithSeperator( final String... p_varargs )
    {
        if ( ( p_varargs == null ) || ( p_varargs.length < 2 ) )
            throw new CIllegalArgumentException( CCommon.languagestring( CPath.class, "createpath" ) );

        return new CPath(
            Arrays.asList( p_varargs ).subList( 1, p_varargs.length ).stream()
                  .flatMap( i -> Arrays.stream( StringUtils.split( i, p_varargs[0] ) ) )
        );
    }

    /**
     * factor method to build path
     *
     * @param p_string input string
     * @return path
     */
    public static IPath from( final String p_string )
    {
        return ( p_string == null ) || ( p_string.isEmpty() ) ? EMPTY : createPathWithSeperator( DEFAULTSEPERATOR, p_string );
    }

    @Override
    public final IPath append( final IPath p_path )
    {
        return new CPath( this ).pushback( p_path );
    }

    @Override
    public final IPath append( final String p_path )
    {
        return new CPath( this ).pushback( p_path );
    }

    @Override
    public final IPath remove( final int p_index )
    {
        m_path.remove( p_index );
        return this;
    }

    @Override
    public final IPath remove( final int p_start, final int p_end )
    {
        m_path.subList( p_start, p_end ).clear();
        return this;
    }

    @Override
    public final synchronized boolean endsWith( final IPath p_path )
    {
        return p_path.size() <= this.size()
               && IntStream.range( 0, p_path.size() ).boxed().parallel().allMatch( i -> this.get( i - p_path.size() ).equals( p_path.get( i ) ) );
    }

    @Override
    public final boolean startsWith( final IPath p_path )
    {
        return p_path.size() <= this.size()
               && IntStream.range( 0, p_path.size() ).boxed().parallel().allMatch( i -> this.get( i ).equals( p_path.get( i ) ) );

    }

    @Override
    public final String get( final int p_index )
    {
        return p_index < 0 ? m_path.get( m_path.size() + p_index ) : m_path.get( p_index );
    }

    @Override
    public final String getPath( final String p_separator )
    {
        return StringUtils.join( m_path, p_separator );
    }

    @Override
    public final String getPath()
    {
        return StringUtils.join( m_path, m_separator );
    }

    @Override
    public final String getSeparator()
    {
        return m_separator;
    }

    @Override
    public final IPath setSeparator( final String p_separator )
    {
        if ( ( p_separator == null ) || ( p_separator.isEmpty() ) )
            throw new CIllegalArgumentException( CCommon.languagestring( this, "separatornotempty" ) );

        m_separator = p_separator;
        return this;
    }

    @Override
    public final synchronized IPath toLower()
    {
        IntStream.range( 0, m_path.size() ).boxed().parallel().forEach( i -> m_path.set( i, m_path.get( i ).toLowerCase() ) );
        return this;
    }

    @Override
    public final synchronized IPath toUpper()
    {
        IntStream.range( 0, m_path.size() ).boxed().parallel().forEach( i -> m_path.set( i, m_path.get( i ).toUpperCase() ) );
        return this;
    }

    @Override
    public final IPath getSubPath( final int p_fromIndex )
    {
        return this.getSubPath( p_fromIndex, this.size() );
    }

    @Override
    public final IPath getSubPath( final int p_fromIndex, final int p_toIndex )
    {
        return new CPath(
            p_toIndex == 0
            ? Stream.of()
            : IntStream.range(
                p_fromIndex,
                p_toIndex > 0 ? p_toIndex : this.size() + p_toIndex
            )
            .mapToObj( m_path::get )
        ).setSeparator( m_separator );
    }

    @Override
    public final synchronized String getSuffix()
    {
        return m_path.isEmpty()
               ? ""
               : m_path.get( m_path.size() - 1 );
    }

    @Override
    public final int hashCode()
    {
        return m_path.stream().mapToInt( String::hashCode ).sum();
    }

    @Override
    @SuppressFBWarnings( "EQ_CHECK_FOR_OPERAND_NOT_COMPATIBLE_WITH_THIS" )
    public final boolean equals( final Object p_object )
    {
        return ( p_object != null )
               && (
                    ( ( p_object instanceof IPath ) && ( this.hashCode() == p_object.hashCode() ) )
                    || ( ( p_object instanceof String ) && ( this.getPath().hashCode() == p_object.hashCode() ) )
               );
    }

    @Override
    public final String toString()
    {
        return this.getPath();
    }

    /**
     * check if the path is empty
     *
     * @return empty flag
     */
    public final boolean isEmpty()
    {
        return m_path.isEmpty();
    }

    @Override
    public final IPath pushback( final IPath p_path )
    {
        p_path.stream().forEach( m_path::add );
        return this;
    }

    @Override
    public final IPath pushback( final String p_path )
    {
        this.pushback( new CPath( p_path ) );
        return this;
    }

    @Override
    public final IPath pushfront( final String p_path )
    {
        this.pushfront( new CPath( p_path ) );
        return this;
    }

    @Override
    public final synchronized IPath pushfront( final IPath p_path )
    {
        final List<String> l_path = Stream.concat( p_path.stream(), m_path.stream() ).collect( Collectors.toList() );
        m_path.clear();
        l_path.forEach( m_path::add );
        return this;
    }

    @Override
    public final String removeSuffix()
    {
        if ( this.isEmpty() )
            return null;

        final String l_suffix = this.getSuffix();
        if ( m_path.size() > 0 )
            m_path.remove( m_path.size() - 1 );
        return l_suffix;
    }

    @Override
    public final IPath reverse()
    {
        Collections.reverse( m_path );
        return this;
    }

    @Override
    public final int size()
    {
        return m_path.size();
    }

    @Override
    public final boolean startsWith( final String p_path )
    {
        return this.startsWith( new CPath( p_path ) );
    }

    @Override
    public final Stream<String> stream()
    {
        return m_path.stream();
    }

    @Override
    public final Stream<String> parallelStream()
    {
        return m_path.parallelStream();
    }

    @Override
    public final int compareTo( final IPath p_path )
    {
        return Integer.compare( this.hashCode(), p_path.hashCode() );
    }

    /**
     * normalize the internal path
     */
    private synchronized void normalize()
    {
        if ( m_path.isEmpty() )
            return;

        // create path-copy and nomalize (remove dot, double-dot and empty values)
        final List<String> l_dotremove = m_path.stream()
                                               .filter( i -> ( i != null ) && ( !i.isEmpty() ) && ( !".".equals( i ) ) )
                                               .collect( Collectors.toList() );
        if ( l_dotremove.isEmpty() )
            return;

        final String l_last = l_dotremove.get( l_dotremove.size() - 1 );
        final List<String> l_backremove = IntStream.range( 0, l_dotremove.size() - 1 )
                                                   .boxed()
                                                   .filter( i -> !l_dotremove.get( i + 1 ).equals( ".." ) )
                                                   .map( l_dotremove::get )
                                                   .collect( Collectors.toList() );
        if ( !"..".equals( l_last ) )
            l_backremove.add( l_last );

        // clear internal path and add optimized path
        m_path.clear();
        l_backremove.forEach( m_path::add );
    }

    /**
     * collector factory
     *
     * @return collector
     */
    private static Collector<String, List<String>, List<String>> collectorfactory()
    {
        return new Collector<String, List<String>, List<String>>()
        {
            @Override
            public final Supplier<List<String>> supplier()
            {
                return CopyOnWriteArrayList<String>::new;
            }

            @Override
            public final BiConsumer<List<String>, String> accumulator()
            {
                return List::add;
            }

            @Override
            public final BinaryOperator<List<String>> combiner()
            {
                return (i, j) -> {
                    i.addAll( j );
                    return i;
                };
            }

            @Override
            public final Function<List<String>, List<String>> finisher()
            {
                return i -> i;
            }

            @Override
            public final Set<Characteristics> characteristics()
            {
                return Collections.emptySet();
            }
        };
    }

    /**
     * list factory
     *
     * @return list
     */
    private static List<String> listfactory()
    {
        return new CopyOnWriteArrayList<>();
    }

}
