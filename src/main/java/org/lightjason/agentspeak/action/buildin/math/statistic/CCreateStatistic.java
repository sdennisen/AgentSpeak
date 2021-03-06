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

package org.lightjason.agentspeak.action.buildin.math.statistic;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.stat.descriptive.SynchronizedDescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SynchronizedSummaryStatistics;
import org.lightjason.agentspeak.action.buildin.IBuildinAction;
import org.lightjason.agentspeak.error.CIllegalStateException;
import org.lightjason.agentspeak.language.CRawTerm;
import org.lightjason.agentspeak.language.ITerm;
import org.lightjason.agentspeak.language.execution.IContext;
import org.lightjason.agentspeak.language.execution.fuzzy.CFuzzyValue;
import org.lightjason.agentspeak.language.execution.fuzzy.IFuzzyValue;

import java.util.List;
import java.util.Locale;


/**
 * action to create a summary statistic
 */
public final class CCreateStatistic extends IBuildinAction
{

    /**
     * ctor
     */
    public CCreateStatistic()
    {
        super( 3 );
    }

    @Override
    public final int minimalArgumentNumber()
    {
        return 0;
    }

    @Override
    public final IFuzzyValue<Boolean> execute( final IContext p_context, final boolean p_parallel, final List<ITerm> p_argument, final List<ITerm> p_return,
                                               final List<ITerm> p_annotation
    )
    {
        p_return.add( CRawTerm.from(
            ( p_argument.size() == 0
              ? EType.SUMMARY
              : EType.from( p_argument.get( 0 ).<String>raw() )
            ).generate( p_parallel )
        ) );

        return CFuzzyValue.from( true );
    }


    /**
     * enume statistic type
     */
    private enum EType
    {
        SUMMARY,
        DESCRIPTIVE;

        /**
         * additional factory
         *
         * @param p_value string
         * @return enum
         */
        public static EType from( final String p_value )
        {
            return EType.valueOf( p_value.trim().toUpperCase( Locale.ROOT ) );
        }

        /**
         * returns the statistic object
         *
         * @param p_parallel parallel-safe
         * @return statistic object
         */
        public final StatisticalSummary generate( final Boolean p_parallel )
        {
            switch ( this )
            {
                case SUMMARY:
                    return p_parallel
                           ? new SynchronizedSummaryStatistics()
                           : new SummaryStatistics();

                case DESCRIPTIVE:
                    return p_parallel
                           ? new SynchronizedDescriptiveStatistics()
                           : new DescriptiveStatistics();

                default:
                    throw new CIllegalStateException( org.lightjason.agentspeak.common.CCommon.languagestring( this, "unknown", this ) );
            }
        }
    }
}
