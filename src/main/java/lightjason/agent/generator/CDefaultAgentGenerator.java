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

package lightjason.agent.generator;

import lightjason.agent.CAgent;
import lightjason.agent.IAgent;
import lightjason.agent.action.IAction;
import lightjason.agent.configuration.CDefaultAgentConfiguration;
import lightjason.agent.configuration.IAgentConfiguration;
import lightjason.agent.fuzzy.CBoolFuzzy;
import lightjason.beliefbase.IBeliefBaseUpdate;
import lightjason.common.CCommon;
import lightjason.grammar.CParserAgent;
import lightjason.grammar.IASTVisitorAgent;
import lightjason.language.execution.IVariableBuilder;
import lightjason.language.execution.action.unify.IUnifier;
import lightjason.language.score.IAggregation;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * agent generator
 */
public class CDefaultAgentGenerator implements IAgentGenerator
{
    /**
     * logger
     */
    protected static final Logger LOGGER = CCommon.getLogger( CDefaultAgentGenerator.class );
    /**
     * configuration of an agent
     */
    protected final IAgentConfiguration m_configuration;


    /**
     * ctor
     *
     * @param p_stream input stream
     * @param p_actions set with action
     * @param p_unifier unifier component
     * @param p_aggregation aggregation function
     * @throws Exception thrown on error
     */
    public CDefaultAgentGenerator( final InputStream p_stream, final Set<IAction> p_actions, final IUnifier p_unifier, final IAggregation p_aggregation )
    throws Exception
    {
        this( p_stream, p_actions, p_unifier, p_aggregation, null, null );
    }

    /**
     * ctor
     *
     * @param p_stream input stream
     * @param p_actions set with action
     * @param p_unifier unifier component
     * @param p_aggregation aggregation function
     * @param p_beliefbaseupdate beliefbase updater
     * @param p_variablebuilder variable builder (can be set to null)
     * @throws Exception thrown on error
     */
    public CDefaultAgentGenerator( final InputStream p_stream, final Set<IAction> p_actions, final IUnifier p_unifier,
                                   final IAggregation p_aggregation, final IBeliefBaseUpdate p_beliefbaseupdate,
                                   final IVariableBuilder p_variablebuilder
    )
    throws Exception
    {
        final IASTVisitorAgent l_visitor = new CParserAgent( p_actions ).parse( p_stream );

        // build configuration (configuration runs cloning of objects if needed)
        m_configuration = new CDefaultAgentConfiguration(
                new CBoolFuzzy(),
                l_visitor.getInitialBeliefs(),
                p_beliefbaseupdate,
                l_visitor.getPlans(),
                l_visitor.getRules(),
                l_visitor.getInitialGoal(),
                p_unifier,
                p_aggregation,
                p_variablebuilder
        );
    }

    @Override
    public <T> IAgent generate( final T... p_data ) throws Exception
    {
        LOGGER.info( MessageFormat.format( "generate agent: {0}", Arrays.toString( p_data ) ) );
        return new CAgent( m_configuration );
    }

    @Override
    public <T> Set<IAgent> generate( final int p_number, final T... p_data )
    {
        return IntStream.range( 0, p_number ).parallel().mapToObj( i -> {
            try
            {
                return this.generate( p_data );
            }
            catch ( final Exception l_exception )
            {
                return null;
            }
        } ).filter( i -> i != null ).collect( Collectors.toSet() );
    }

}
