/**
 * @cond LICENSE
 * ######################################################################################
 * # LGPL License                                                                       #
 * #                                                                                    #
 * # This file is part of the Light-Jason                                               #
 * # Copyright (c) 2015-16, Philipp Kraus (philipp.kraus@tu-clausthal.de)               #
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

package lightjason.language.variable;

import lightjason.common.CPath;
import lightjason.common.IPath;
import lightjason.error.CIllegalArgumentException;
import lightjason.error.CIllegalStateException;
import lightjason.language.ITerm;

import java.text.MessageFormat;
import java.util.Arrays;


/**
 * default variable definition
 *
 * @note variable set is not thread-safe on default
 * @todo deep-copy value copy incorrect
 */
public class CVariable<T> implements IVariable<T>
{
    /**
     * variable / functor name
     */
    protected final IPath m_functor;
    /**
     * boolean flag, that defines an variable which matchs always
     */
    protected final boolean m_any;
    /**
     * value of the variable
     */
    protected T m_value;

    /**
     * ctor
     *
     * @param p_functor name
     */
    public CVariable( final String p_functor )
    {
        this( CPath.from( p_functor ), null );
    }

    /**
     * ctor
     *
     * @param p_functor name
     * @param p_value value
     */
    public CVariable( final String p_functor, final T p_value )
    {
        this( CPath.from( p_functor ), p_value );
    }

    /**
     * ctor
     *
     * @param p_functor name
     */
    public CVariable( final IPath p_functor )
    {
        this( p_functor, null );
    }

    /**
     * ctor
     *
     * @param p_functor name
     * @param p_value value
     */
    public CVariable( final IPath p_functor, final T p_value )
    {
        m_any = ( p_functor == null ) || p_functor.isEmpty() || p_functor.equals( "_" );
        m_functor = p_functor;
        this.internalset( p_value );
    }

    @Override
    public IVariable<T> set( final T p_value )
    {
        return this.internalset( p_value );
    }

    @Override
    public T get()
    {
        return m_value;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <N> N getTyped()
    {
        return (N) m_value;
    }

    @Override
    public boolean isAllocated()
    {
        return m_value != null;
    }

    @Override
    public final boolean isAny()
    {
        return m_any;
    }

    @Override
    public boolean hasMutex()
    {
        return false;
    }

    @Override
    public IVariable<T> throwNotAllocated() throws IllegalStateException
    {
        if ( !this.isAllocated() )
            throw new CIllegalStateException( lightjason.common.CCommon.getLanguageString( CVariable.class, "notallocated", this ) );

        return this;
    }

    @Override
    public boolean isValueAssignableTo( final Class<?>... p_class )
    {
        return m_value == null ? true : Arrays.asList( p_class ).stream().map( i -> i.isAssignableFrom( m_value.getClass() ) ).anyMatch( i -> i );
    }

    @Override
    public IVariable<T> throwValueNotAssignableTo( final Class<?>... p_class ) throws IllegalArgumentException
    {
        if ( !this.isValueAssignableTo( p_class ) )
            throw new CIllegalArgumentException(
                    lightjason.common.CCommon.getLanguageString( CVariable.class, "notassignable", this, Arrays.asList( p_class ) ) );

        return this;
    }

    @Override
    public int hashCode()
    {
        return m_functor.hashCode();
    }

    @Override
    public boolean equals( final Object p_object )
    {
        return this.hashCode() == p_object.hashCode();
    }

    @Override
    public String toString()
    {
        return MessageFormat.format( "{0}({1})", m_functor, m_value == null ? "" : m_value );
    }

    @Override
    public final String getFunctor()
    {
        return m_functor.getSuffix();
    }

    @Override
    public final IPath getFunctorPath()
    {
        return m_functor.getSubPath( 0, m_functor.size() - 1 );
    }

    @Override
    public final IPath getFQNFunctor()
    {
        return m_functor;
    }

    @Override
    public IVariable<T> shallowcopy( final IPath... p_prefix )
    {
        return ( p_prefix == null ) || ( p_prefix.length == 0 )
               ? new CVariable<T>( m_functor, m_value )
               : new CVariable<T>( p_prefix[0].append( m_functor ), m_value );
    }


    @Override
    public IVariable<T> shallowcopySuffix()
    {
        return new CVariable<T>( m_functor.getSuffix(), m_value );
    }

    @Override
    public final ITerm deepcopy( final IPath... p_prefix )
    {
        return this.shallowcopy( p_prefix );
    }

    @Override
    public final ITerm deepcopySuffix()
    {
        return this.shallowcopySuffix();
    }

    /**
     * internel set for avoid any exception throwing
     *
     * @param p_value value
     * @return self reference
     */
    protected final IVariable<T> internalset( final T p_value )
    {
        // value must be set manually to avoid exception throwing (see CVariable.set)
        if ( !m_any )
            m_value = lightjason.language.CCommon.getRawValue( p_value );
        return this;
    }
}