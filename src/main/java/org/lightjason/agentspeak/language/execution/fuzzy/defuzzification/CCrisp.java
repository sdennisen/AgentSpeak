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

package org.lightjason.agentspeak.language.execution.fuzzy.defuzzification;

import org.lightjason.agentspeak.agent.IAgent;
import org.lightjason.agentspeak.language.execution.fuzzy.IFuzzyValue;
import org.lightjason.agentspeak.language.execution.fuzzy.operator.IFuzzyComplement;


/**
 * defuzzification to a crisp value
 *
 * @tparam T fuzzy type
 * @tparam S agent type
 */
public class CCrisp<T, S extends IAgent<?>> implements IDefuzzification<T, S>
{

    /**
     * fuzzy complement
     */
    private final IFuzzyComplement<T> m_complement;

    /**
     * ctor
     *
     * @param p_complement fuzzy complement operator
     */
    public CCrisp( final IFuzzyComplement<T> p_complement )
    {
        m_complement = p_complement;
    }


    @Override
    public final T defuzzify( final IFuzzyValue<T> p_value )
    {
        return p_value.fuzzy() <= 0.5 ? m_complement.complement( p_value ).value() : p_value.value();
    }

    @Override
    public S update( final S p_agent )
    {
        return p_agent;
    }
}
