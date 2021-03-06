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

package org.lightjason.agentspeak.language.execution.action.achievement_test;

import org.lightjason.agentspeak.agent.IAgent;
import org.lightjason.agentspeak.language.CCommon;
import org.lightjason.agentspeak.language.ILiteral;
import org.lightjason.agentspeak.language.ITerm;
import org.lightjason.agentspeak.language.execution.IContext;
import org.lightjason.agentspeak.language.execution.fuzzy.IFuzzyValue;
import org.lightjason.agentspeak.language.instantiable.rule.IRule;
import org.lightjason.agentspeak.language.variable.IVariable;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;


/**
 * achievement for rule-literal execution
 */
public final class CAchievementRuleLiteral extends IAchievementRule<ILiteral>
{

    /**
     * ctor
     *
     * @param p_literal literal of the call
     */
    public CAchievementRuleLiteral( final ILiteral p_literal )
    {
        super( p_literal );
    }

    @Override
    public final IFuzzyValue<Boolean> execute( final IContext p_context, final boolean p_parallel, final List<ITerm> p_argument, final List<ITerm> p_return,
                                               final List<ITerm> p_annotation
    )
    {
        return CAchievementRuleLiteral.execute( p_context, m_value, m_value.hasAt() );
    }

    @Override
    public final double score( final IAgent<?> p_agent )
    {
        // rules can create a cyclic reference so on calculate the score value
        // a cyclic reference must be ignored
        final Collection<IRule> l_rules = p_agent.rules().get( m_value.fqnfunctor() );
        return l_rules == null
               ? p_agent.aggregation().error()
               : l_rules.parallelStream()
                        .filter( this::equals )
                        .mapToDouble( i -> i.score( p_agent ) )
                        .sum();
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public final Stream<IVariable<?>> variables()
    {
        return Stream.concat(
            CCommon.recursiveterm( m_value.orderedvalues() ),
            CCommon.recursiveliteral( m_value.annotations() )
        )
                     .parallel()
                     .filter( i -> i instanceof IVariable<?> )
                     .map( i -> (IVariable<?>) i );
    }

    @Override
    public final String toString()
    {
        return MessageFormat.format( "$", m_value );
    }

}
