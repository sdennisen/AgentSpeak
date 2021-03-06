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

package org.lightjason.agentspeak.action.buildin.collection.list;

import org.lightjason.agentspeak.action.buildin.IBuildinAction;
import org.lightjason.agentspeak.language.CCommon;
import org.lightjason.agentspeak.language.CRawTerm;
import org.lightjason.agentspeak.language.ITerm;
import org.lightjason.agentspeak.language.execution.IContext;
import org.lightjason.agentspeak.language.execution.fuzzy.CFuzzyValue;
import org.lightjason.agentspeak.language.execution.fuzzy.IFuzzyValue;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


/**
 * creates the intersection between lists.
 * All arguments are lists and the action returns the
 * intersection \f$ \cap M_i \forall i \in \mathbb{N} \f$, the action fails never
 * @code I = collection/list/intersect( [1,2,[3,4]], [3,4,[8,9]], [1,2,3,5] ); @endcode
 */
public final class CIntersect extends IBuildinAction
{
    /**
     * ctor
     */
    public CIntersect()
    {
        super( 3 );
    }

    @Override
    public final int minimalArgumentNumber()
    {
        return 2;
    }

    @Override
    public final IFuzzyValue<Boolean> execute( final IContext p_context, final boolean p_parallel, final List<ITerm> p_argument, final List<ITerm> p_return,
                                               final List<ITerm> p_annotation
    )
    {
        // all arguments must be lists (build unique list of all elements and check all collection if an element exists in each collection)
        final List<?> l_result = CCommon.flatcollection( p_argument ).parallelStream()
                                        .map( ITerm::raw )
                                        .distinct()
                                        .filter(
                                            i -> p_argument.parallelStream()
                                                           .allMatch( j -> j.<Collection<ITerm>>raw()
                                                               .parallelStream()
                                                               .map( ITerm::raw )
                                                               .collect( Collectors.toList() )
                                                               .contains( i )
                                                           )
                                        ).collect( Collectors.toList() );

        p_return.add( CRawTerm.from(
            p_parallel ? Collections.synchronizedList( l_result ) : l_result
        ) );

        return CFuzzyValue.from( true );
    }

}
