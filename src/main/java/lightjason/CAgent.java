/**
 * @cond LICENSE
 * ######################################################################################
 * # GPL License                                                                        #
 * #                                                                                    #
 * # This file is part of the Light-Jason                                               #
 * # Copyright (c) 2015, Philipp Kraus (philipp.kraus@tu-clausthal.de)                  #
 * # This program is free software: you can redistribute it and/or modify               #
 * # it under the terms of the GNU General Public License as                            #
 * # published by the Free Software Foundation, either version 3 of the                 #
 * # License, or (at your option) any later version.                                    #
 * #                                                                                    #
 * # This program is distributed in the hope that it will be useful,                    #
 * # but WITHOUT ANY WARRANTY; without even the implied warranty of                     #
 * # MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                      #
 * # GNU General Public License for more details.                                       #
 * #                                                                                    #
 * # You should have received a copy of the GNU General Public License                  #
 * # along with this program. If not, see http://www.gnu.org/licenses/                  #
 * ######################################################################################
 * @endcond
 */

package lightjason;

import lightjason.runtime.IAction;
import lightjason.runtime.IPlan;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class CAgent implements IAgent
{
    /**
     * agent name
     */
    protected final String m_name;
    /**
     * thread-safe map with all existing plans
     *
     * @note plan list ust be a linked-hashset
     * to store the execution order of the plans
     */
    protected final Map<String, Set<IPlan>> m_plans = new ConcurrentHashMap<>();
    /**
     * beliefbase of the agent
     */
    protected final IBeliefBase m_beliefbase;
    /**
     * curent agent cycle
     */
    protected int m_cycle = 0;
    /**
     * suspending state
     */
    private volatile boolean m_suspend = false;


    /**
     * ctor
     *
     * @param p_action map with all usable actions
     * @param p_action map with all usable actions
     * @param p_stream input stream with agent-speak source
     * @throws IOException is throwing on parsing error
     */
    public CAgent( final Map<String, IAction> p_action, final InputStream p_stream ) throws IOException
    {
        this( p_action, p_stream, null, null );
    }

    /**
     * ctor
     *
     * @param p_action map with all usable actions
     * @param p_stream input stream with agent-speak source
     * @param p_beliefbase beliefbase
     * @throws IOException is throwing on parsing error
     */
    public CAgent( final Map<String, IAction> p_action, final InputStream p_stream, final IBeliefBase p_beliefbase ) throws IOException
    {
        this( p_action, p_stream, p_beliefbase, null );
    }

    /**
     * ctor
     *
     * @param p_action map with all usable actions
     * @param p_stream input stream with agent-speak source
     * @param p_name agent name
     * @throws IOException is throwing on parsing error
     */
    public CAgent( final Map<String, IAction> p_action, final InputStream p_stream, final String p_name ) throws IOException
    {
        this( p_action, p_stream, null, p_name );
    }

    /**
     * ctor
     *
     * @param p_action map with all usable actions
     * @param p_stream input stream with agent-speak source
     * @param p_beliefbase beliefbase
     * @param p_name agent name
     * @throws IOException is throwing on parsing error
     */
    public CAgent( final Map<String, IAction> p_action, final InputStream p_stream, final IBeliefBase p_beliefbase, final String p_name ) throws IOException
    {
        // initialize agent
        m_beliefbase = p_beliefbase == null ? null : p_beliefbase;
        m_name = ( p_name == null ) || ( p_name.isEmpty() ) ? this.toString() : p_name;

        // parse AgentSpeak syntax
        final lightjason.JasonParser.AgentContext l_script = new lightjason.JasonParser(
                new CommonTokenStream( new lightjason.JasonLexer( new ANTLRInputStream( p_stream ) ) )
        ).agent();
    }


    @Override
    public final int getCycle()
    {
        return m_cycle;
    }

    @Override
    public final String getName()
    {
        return m_name;
    }

    @Override
    public IBeliefBase getBeliefBase()
    {
        return m_beliefbase;
    }

    @Override
    public void trigger( final String p_goal )
    {

    }

    @Override
    public final synchronized void suspend()
    {
        m_suspend = true;
    }

    @Override
    public final boolean isSuspending()
    {
        return m_suspend;
    }

    @Override
    public final synchronized void resume()
    {
        m_suspend = false;
    }

    @Override
    public IAgent clone( IBeliefBase p_beliefbase )
    {
        return null;
    }

    @Override
    public IAgent clone()
    {
        return null;
    }

    @Override
    public IAgent call() throws Exception
    {
        // run beliefbase update, because
        // environment can be changed
        m_beliefbase.update();
        if ( m_suspend )
            return this;

        // increment cycle
        m_cycle++;

        return this;
    }

}
