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

package org.lightjason.agentspeak.agent;

import org.junit.Before;
import org.junit.Test;
import org.lightjason.agentspeak.action.IBaseAction;
import org.lightjason.agentspeak.common.CCommon;
import org.lightjason.agentspeak.common.CPath;
import org.lightjason.agentspeak.common.IPath;
import org.lightjason.agentspeak.configuration.IAgentConfiguration;
import org.lightjason.agentspeak.generator.IBaseAgentGenerator;
import org.lightjason.agentspeak.language.CLiteral;
import org.lightjason.agentspeak.language.CRawTerm;
import org.lightjason.agentspeak.language.ITerm;
import org.lightjason.agentspeak.language.execution.IContext;
import org.lightjason.agentspeak.language.execution.fuzzy.CFuzzyValue;
import org.lightjason.agentspeak.language.execution.fuzzy.IFuzzyValue;
import org.lightjason.agentspeak.language.instantiable.plan.trigger.CTrigger;
import org.lightjason.agentspeak.language.instantiable.plan.trigger.ITrigger;
import org.lightjason.agentspeak.language.score.IAggregation;

import java.io.FileInputStream;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;


/**
 * test for playing towers of
 * hanoi with an agent
 * @see https://en.wikipedia.org/wiki/Tower_of_Hanoi
 */
public final class TestCHanoiTowers
{
    /**
     * asl file
     */
    private static final String ASL = "src/test/resources/agent/hanoi.asl";
    /**
     * number of towers
     **/
    private static final int TOWERNUMBER = 3;
    /**
     * number of slices
     */
    private static final int SLICENUMBER = 3;
    /**
     * number of agents
     */
    private static final int AGENTNUMBER = 2;
    /**
     * tower map
     */
    private Map<Integer, CTower> m_tower;
    /**
     * agent map
     */
    private Map<Integer, CAgent> m_agents;



    /**
     * test initialize
     *
     * @throws Exception on initialize error
     */
    @Before
    public void initialize() throws Exception
    {

        final Map<Integer, CTower> l_towermap = new ConcurrentHashMap<>();
        IntStream.range( 0, TOWERNUMBER )
                 .forEach( i -> {
                     final CTower l_tower = new CTower();
                     l_towermap.put( i, l_tower );
                     if ( i == 0 )
                         IntStream.range( 0, SLICENUMBER ).forEach( j -> l_tower.push( new CSlice( SLICENUMBER - j ) ) );
                 } );
        m_tower = Collections.unmodifiableMap( l_towermap );


        final Map<Integer, CAgent> l_agentmap = new ConcurrentHashMap<>();
        try
        (
            final InputStream l_asl = new FileInputStream( ASL );
        )
        {
            final CGenerator l_generator = new CGenerator( l_asl );
            IntStream.range( 0, AGENTNUMBER )
                     .forEach( i -> l_agentmap.put( i, l_generator.generatesingle( i ) ) );
        } catch ( final Exception l_exception )
        {
            assertTrue( "asl could not be read", true );
        }
        m_agents = Collections.unmodifiableMap( l_agentmap );

    }



    /**
     * running towers of hanoi
     */
    @Test
    public final void play()
    {
        IntStream.range( 0, 10 )
            .forEach( i -> m_agents.values()
                                   .parallelStream()
                                   .forEach( j -> {
                                       try
                                       {
                                           j.call();
                                       }
                                       catch ( final Exception l_exception )
                                       {
                                           l_exception.printStackTrace();
                                       }
                                   } )
            );
    }


    /**
     * agent generator
     */
    private final class CGenerator extends IBaseAgentGenerator<CAgent>
    {
        /**
         * ctor
         *
         * @param p_stream asl stream
         * @throws Exception on any error
         */
        CGenerator( final InputStream p_stream ) throws Exception
        {
            super( p_stream,
                   Stream.concat(
                    CCommon.actionsFromPackage(),
                    Stream.of(
                        new CTowerPush( 0.25 ),
                        new CTowerPop(),
                        new CCompareSlice( 0.2 ),
                        new CSend()
                    )
                   ).collect( Collectors.toSet() ),
                   IAggregation.EMPTY
            );
        }

        @Override
        @SuppressWarnings( "unchecked" )
        public final CAgent generatesingle( final Object... p_data )
        {
            return new CAgent( (int) p_data[0], m_configuration );
        }
    }


    /**
     * pushs a slice to a tower
     */
    private final class CTowerPush extends IBaseAction
    {
        /**
         * probability for action failing
         */
        private final double m_failprobability;

        /**
         * ctor
         *
         * @param p_failprobability failing probability
         */
        CTowerPush( final double p_failprobability )
        {
            m_failprobability = p_failprobability;
        }

        @Override
        public final IPath name()
        {
            return CPath.from( "tower/push" );
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
            final CTower l_tower = m_tower.get( p_argument.get( 0 ).<Integer>raw() );
            if (  ( l_tower == null ) || ( Math.random() < m_failprobability )  )
                return CFuzzyValue.from( false );

            try
            {
                l_tower.push( p_argument.get( 1 ).<CSlice>raw() );
                return CFuzzyValue.from( true );
            }
            catch ( final Exception l_exception )
            {
                return CFuzzyValue.from( false );
            }
        }
    }

    /**
     * pops an elements from a tower
     */
    private final class CTowerPop extends IBaseAction
    {

        @Override
        public final IPath name()
        {
            return CPath.from( "tower/pop" );
        }

        @Override
        public final int minimalArgumentNumber()
        {
            return 1;
        }

        @Override
        public final IFuzzyValue<Boolean> execute( final IContext p_context, final boolean p_parallel, final List<ITerm> p_argument, final List<ITerm> p_return,
                                             final List<ITerm> p_annotation
        )
        {
            final CTower l_tower = m_tower.get( p_argument.get( 0 ).<Number>raw().intValue() );
            if ( l_tower == null )
                return CFuzzyValue.from( false );

            try
            {
                p_return.add( CRawTerm.from( l_tower.pop() ) );
                return CFuzzyValue.from( true );
            } catch (final Exception l_exception)
            {
                return CFuzzyValue.from( false );
            }
        }
    }

    /**
     * compare action for slices with probability
     * to do it wrong
     */
    private static final class CCompareSlice extends IBaseAction
    {
        /**
         * probability to get a wrong result
         */
        private final double m_invertprobability;

        /**
         * ctor
         * @param p_invertprobability probability to invert result
         */
        CCompareSlice( final double p_invertprobability )
        {
            m_invertprobability = p_invertprobability;
        }

        @Override
        public final IPath name()
        {
            return CPath.from( "slice/less" );
        }

        @Override
        public final int minimalArgumentNumber()
        {
            return 2;
        }

        @Override
        public IFuzzyValue<Boolean> execute( final IContext p_context, final boolean p_parallel, final List<ITerm> p_argument, final List<ITerm> p_return,
                                             final List<ITerm> p_annotation
        )
        {
            return CFuzzyValue.from(
                Math.random() < m_invertprobability != p_argument.get( 0 ).<CSlice>raw().size() < p_argument.get( 1 ).<CSlice>raw().size()
            );
        }
    }

    /**
     * action for communication between agents
     */
    private final class CSend extends IBaseAction
    {

        @Override
        public final IPath name()
        {
            return CPath.from( "message/send"  );
        }

        @Override
        public final int minimalArgumentNumber()
        {
            return 2;
        }

        @Override
        @SuppressWarnings( "unchcked" )
        public final IFuzzyValue<Boolean> execute( final IContext p_context, final boolean p_parallel, final List<ITerm> p_argument, final List<ITerm> p_return,
                                             final List<ITerm> p_annotation
        )
        {
            final CAgent l_receiver = m_agents.get( p_argument.get( 0 ).<Integer>raw() );
            if ( l_receiver == null )
                return CFuzzyValue.from(  false  );

            l_receiver.trigger(
                CTrigger.from(
                    ITrigger.EType.ADDGOAL,
                    CLiteral.from( "receive",
                                    CLiteral.from(
                                        "message",
                                        p_argument.get( 1 )
                                    ),
                                    CLiteral.from( "from", CRawTerm.from( ( (CAgent) p_context.agent() ).id() ) )
                    )
                )
            );
            return CFuzzyValue.from( true );
        }

    }


    /**
     * agent class
     */
    private class CAgent extends IBaseAgent<CAgent>
    {
        /**
         * id of the agent
         */
        private final int m_id;

        /**
         * ctor
         *
         * @param p_id id of the agent
         * @param p_configuration agent configuration
         */
        CAgent( final int p_id, final IAgentConfiguration<CAgent> p_configuration )
        {
            super( p_configuration );
            m_id = p_id;
        }

        /**
         * returns the id of the agent
         *
         * @return id
         */
        final int id()
        {
            return m_id;
        }
    }


    /**
     * defines a slice
     */
    private static final class CSlice
    {
        /**
         * slice size
         */
        private final int m_size;

        /**
         * ctor
         *
         * @param p_size slice size
         */
        CSlice( final int p_size )
        {
            m_size = p_size;
        }

        /**
         * returns the size
         * @return slice size
         */
        final int size()
        {
            return m_size;
        }

        @Override
        public String toString()
        {
            return MessageFormat.format( "slice {0}", m_size );
        }
    }

    /**
     * tower
     */
    private static final class CTower extends Stack<CSlice>
    {
        @Override
        public synchronized CSlice push( final CSlice p_item )
        {
            if (  ( this.size() > 0 ) && ( this.peek().size() < p_item.size() )  )
                throw new IllegalArgumentException();

            return super.push( p_item );
        }

        @Override
        public synchronized CSlice pop()
        {
            if ( this.isEmpty() )
                throw new IllegalStateException();

            return super.pop();
        }

        @Override
        public synchronized CSlice peek()
        {
            return super.peek();
        }

        @Override
        public synchronized boolean empty()
        {
            return super.empty();
        }

        @Override
        public synchronized int search( final Object o )
        {
            return super.search( o );
        }
    }


    /**
     * main method for manual test
     * @param p_args CLI arguments
     * @throws Exception is thrown on any error
     */
    public static void main( final String[] p_args ) throws Exception
    {
        final TestCHanoiTowers l_hanoi = new TestCHanoiTowers();
        l_hanoi.initialize();
        l_hanoi.play();
    }
}
