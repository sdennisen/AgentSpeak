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

package org.lightjason.agentspeak.action.buildin.rest;

import org.lightjason.agentspeak.language.CLiteral;
import org.lightjason.agentspeak.language.ITerm;
import org.lightjason.agentspeak.language.execution.IContext;
import org.lightjason.agentspeak.language.execution.fuzzy.CFuzzyValue;
import org.lightjason.agentspeak.language.execution.fuzzy.IFuzzyValue;

import java.io.IOException;
import java.util.List;
import java.util.Map;


/**
 * action to call a restful webservice with XML data.
 * Creates a literal from an XML REST-webservice, first argument
 * is the URL of the webservice and second argument the functor of the literal
 * @code W = rest/xmlobject( "https://en.wikipedia.org/wiki/Special:Export/AgentSpeak", "wikipedia" ); @endcode
 *
 * @see https://en.wikipedia.org/wiki/Representational_state_transfer
 * @see https://en.wikipedia.org/wiki/Web_service
 * @see https://en.wikipedia.org/wiki/XML
 */
public class CXMLObject extends IBaseRest
{
    @Override
    public final IFuzzyValue<Boolean> execute( final IContext p_context, final boolean p_parallel, final List<ITerm> p_argument, final List<ITerm> p_return,
                                         final List<ITerm> p_annotation
    )
    {
        try
        {
            final Map<String, ?> l_data = IBaseRest.xml( p_argument.get( 0 ).<String>raw() );
            p_return.add(
                p_argument.size() == 2
                ? CLiteral.from( p_argument.get( p_argument.size() - 1 ).<String>raw(), flatterm( l_data ) )
                : IBaseRest.baseliteral(
                    p_argument.subList( 1, p_argument.size() ).stream().map( ITerm::<String>raw ),
                    flatterm( l_data )
                )
            );

            return CFuzzyValue.from( true );
        }
        catch ( final IOException l_exception )
        {
            return CFuzzyValue.from( false );
        }
    }
}
