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

package lightjason.agent.action.buildin.bind;

import lightjason.agent.action.IAction;
import lightjason.agent.action.IBaseAction;
import lightjason.common.CPath;
import lightjason.common.CReflection;
import lightjason.language.CCommon;
import lightjason.language.ITerm;
import lightjason.language.execution.IContext;
import lightjason.language.execution.fuzzy.CBoolean;
import lightjason.language.execution.fuzzy.IFuzzyValue;
import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * action for binding action (via reflection)
 * to other Java objects
 */
public final class CBind<T>
{
    /**
     * package name
     */
    private static final String ACTIONPACKAGENAME = "bind";


    /**
     * ctor
     */
    private CBind()
    {
    }



    /**
     * returns a set of actions which are bind on an object
     *
     * @param p_object object bind
     * @return set with actions
     *
     * @tparam T any object type
     */
    public static <T> Set<IAction> get( final T p_object )
    {
        return get( true, p_object, new CFilter() );
    }


    /**
     * returns a set of actions which are bind on an object
     *
     * @param p_fqnpackage use full-qualified package name
     * @param p_object object bind
     * @return set with actions
     *
     * @tparam T any object type
     */
    public static <T> Set<IAction> get( final boolean p_fqnpackage, final T p_object )
    {
        return get( p_fqnpackage, p_object, new CFilter() );
    }

    /**
     * returns a set of actions which are bind on an object
     *
     * @param p_object object bind
     * @param p_filter method filter object
     * @return set with actions
     *
     * @tparam T any object type
     */
    public static <T> Set<IAction> get( final T p_object, final CReflection.IFilter<Method> p_filter )
    {
        return CReflection.getClassMethods( p_object.getClass(), p_filter ).values().parallelStream().map(
                i -> new CObjectAction<T>( true, i.getMethod(), i.getMinimalArgumentNumber(), p_object ) ).collect( Collectors.toSet() );
    }

    /**
     * returns a set of actions which are bind on an object
     *
     * @param p_fqnpackage use full-qualified package name
     * @param p_object object bind
     * @param p_filter method filter object
     * @return set with actions
     *
     * @tparam T any object type
     */
    public static <T> Set<IAction> get( final boolean p_fqnpackage, final T p_object, final CReflection.IFilter<Method> p_filter )
    {
        return CReflection.getClassMethods( p_object.getClass(), p_filter ).values().parallelStream().map(
                i -> new CObjectAction<T>( p_fqnpackage, i.getMethod(), i.getMinimalArgumentNumber(), p_object ) ).collect( Collectors.toSet() );
    }



    /**
     * returns a set of action which are bind on
     * an object within the agent storage with a fixed storage name
     *
     * @param p_fqnpackage use full-qualified package name
     * @param p_class binding class
     * @param p_storagename storage names
     * @return set with actions
     */
    public static Set<IAction> get( final boolean p_fqnpackage, final Class<?> p_class, final String p_storagename )
    {
        return get( p_fqnpackage, p_class, p_storagename, new CFilter() );
    }

    /**
     * returns a set of action which are bind on
     * an object within the agent storage with a fixed storage name
     *
     * @param p_fqnpackage use full-qualified package name
     * @param p_class binding class
     * @param p_storagename storage names
     * @param p_filter method filter object
     * @return set with actions
     */
    public static Set<IAction> get( final boolean p_fqnpackage, final Class<?> p_class, final String p_storagename, final CReflection.IFilter<Method> p_filter )
    {
        return CReflection.getClassMethods( p_class, p_filter ).values().parallelStream().map(
                i -> new CStorageElementAction( true, p_class, i.getMethod(), i.getMinimalArgumentNumber(), p_storagename ) ).collect( Collectors.toSet() );
    }



    /**
     * returns a set of action which are bind on
     * an object within the agent storage name
     *
     * @param p_class binding class
     * @return set with actions
     */
    public static Set<IAction> get( final Class<?> p_class ) throws IllegalAccessException
    {
        return get( true, p_class, new CFilter() );
    }

    /**
     * returns a set of action which are bind on
     * an object within the agent storage name
     *
     * @param p_fqnpackage use full-qualified package name
     * @param p_class binding class
     * @return set with actions
     */
    public static Set<IAction> get( final boolean p_fqnpackage, final Class<?> p_class ) throws IllegalAccessException
    {
        return get( p_fqnpackage, p_class, new CFilter() );
    }

    /**
     * returns a set of action which are bind on
     * an object within the agent storage name
     *
     * @param p_class binding class
     * @param p_filter method filter object
     * @return set with actions
     */
    public static Set<IAction> get( final Class<?> p_class, final CReflection.IFilter<Method> p_filter ) throws IllegalAccessException
    {
        return get( true, p_class, p_filter );
    }

    /**
     * returns a set of action which are bind on
     * an object within the agent storage name
     *
     * @param p_fqnpackage use full-qualified package name
     * @param p_class binding class
     * @param p_filter method filter object
     * @return set with actions
     */
    public static Set<IAction> get( final boolean p_fqnpackage, final Class<?> p_class, final CReflection.IFilter<Method> p_filter )
    throws IllegalAccessException
    {
        return CReflection.getClassMethods( p_class, p_filter ).values().parallelStream().map(
                i -> new CStorageElementAction( p_fqnpackage, p_class, i.getMethod(), i.getMinimalArgumentNumber() ) ).collect( Collectors.toSet() );
    }



    /**
     * action class of object bind
     *
     * @note useful for static object
     * @tparam T object type
     */
    public static class CObjectAction<T> extends IBaseAction
    {
        /**
         * binded object
         */
        public final T m_bindobject;
        /**
         * action name
         */
        private final CPath m_name;
        /**
         * number of arguments
         */
        private final int m_arguments;


        /**
         * ctor
         *
         * @param p_fqnpackage use full-qualified package name
         * @param p_method method bind
         * @param p_minimalarguments minimal number of arguments
         * @param p_object object bind
         */
        private CObjectAction( final boolean p_fqnpackage, final Method p_method, final int p_minimalarguments, final T p_object )
        {
            m_bindobject = p_object;

            m_arguments = p_minimalarguments;
            m_name = CPath.createSplitPath(
                    ClassUtils.PACKAGE_SEPARATOR, ACTIONPACKAGENAME,
                    p_fqnpackage ? p_object.getClass().getCanonicalName() : p_object.getClass().getSimpleName(), p_method.getName()
            ).toLower();
        }

        @Override
        public final CPath getName()
        {
            return m_name;
        }

        @Override
        public final int getMinimalArgumentNumber()
        {
            return m_arguments;
        }

        /**
         * @bug implement execution
         */
        @Override
        public final IFuzzyValue<Boolean> execute( final IContext<?> p_context, final List<ITerm> p_annotation, final List<ITerm> p_argument,
                                                   final List<ITerm> p_return
        )
        {
            return CBoolean.from( true );
        }

    }



    /**
     * object binding by storage name
     *
     * @note usefull for dynammic binding depend on an element
     * within the agent storage
     */
    public static class CStorageElementAction extends IBaseAction
    {
        /**
         * storage name
         */
        private final String m_storagename;
        /**
         * action name
         */
        private final CPath m_name;
        /**
         * number of arguments
         */
        private final int m_arguments;


        /**
         * ctor
         *
         * @param p_fqnpackage use full-qualified package name
         * @param p_class binding class
         * @param p_method method bind
         * @param p_minimalarguments minimal number of arguments
         */
        private CStorageElementAction( final boolean p_fqnpackage, final Class<?> p_class, final Method p_method, final int p_minimalarguments )
        {
            this( p_fqnpackage, p_class, p_method, p_minimalarguments, null );
        }

        /**
         * ctor
         *
         * @param p_fqnpackage use full-qualified package name
         * @param p_class binding class
         * @param p_method method bind
         * @param p_storagename storage names
         */
        private CStorageElementAction( final boolean p_fqnpackage, final Class<?> p_class, final Method p_method, final int p_minimalarguments,
                                       final String p_storagename
        )
        {
            m_storagename = p_storagename;

            m_arguments = p_minimalarguments + ( m_storagename == null ? 1 : 0 );
            m_name = CPath.createSplitPath(
                    ClassUtils.PACKAGE_SEPARATOR, ACTIONPACKAGENAME, p_fqnpackage ? p_class.getCanonicalName() : p_class.getSimpleName(), p_method.getName() )
                          .toLower();
        }

        @Override
        public final CPath getName()
        {
            return m_name;
        }

        @Override
        public final int getMinimalArgumentNumber()
        {
            return m_arguments;
        }

        /**
         * @bug implement execution
         */
        @Override
        public final IFuzzyValue<Boolean> execute( final IContext<?> p_context, final List<ITerm> p_annotation, final List<ITerm> p_argument,
                                                   final List<ITerm> p_return
        )
        {
            final Object l_reference = p_context.getAgent().getStorage().get(
                    m_storagename != null ? m_storagename : CCommon.getRawValue( p_argument.get( 0 ) ) );
            if ( l_reference == null )
                return CBoolean.from( false );


            return CBoolean.from( true );
        }

    }

}
