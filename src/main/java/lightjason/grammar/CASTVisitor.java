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
 * # along with this program. If not, see <http://www.gnu.org/licenses/>.               #
 * ######################################################################################
 * @endcond
 */

package lightjason.grammar;


import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import lightjason.agent.action.IAction;
import lightjason.common.CCommon;
import lightjason.common.CPath;
import lightjason.error.CIllegalArgumentException;
import lightjason.language.CLiteral;
import lightjason.language.CMutexVariable;
import lightjason.language.CRawTerm;
import lightjason.language.CVariable;
import lightjason.language.ILiteral;
import lightjason.language.ITerm;
import lightjason.language.IVariable;
import lightjason.language.execution.IExecution;
import lightjason.language.execution.action.CAchievementGoal;
import lightjason.language.execution.action.CBeliefAction;
import lightjason.language.execution.action.CProxyAction;
import lightjason.language.execution.action.CRawAction;
import lightjason.language.execution.action.CTestGoal;
import lightjason.language.execution.annotation.CAtomAnnotation;
import lightjason.language.execution.annotation.CNumberAnnotation;
import lightjason.language.execution.annotation.CSymbolicAnnotation;
import lightjason.language.execution.annotation.IAnnotation;
import lightjason.language.execution.unaryoperator.CDecrement;
import lightjason.language.execution.unaryoperator.CIncrement;
import lightjason.language.plan.CPlan;
import lightjason.language.plan.IPlan;
import lightjason.language.plan.trigger.CTrigger;
import lightjason.language.plan.trigger.ITrigger;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * default abstract-syntax-tree (AST) visitor for plan-bundles and agent scripts
 *
 * @note methods are implemented twice agent and plan-bundle, because both use equal
 * AgentSpeak(L) grammer, but AntLR visitor does not support inheritance by the grammar definition
 */
@SuppressWarnings( {"all", "warnings", "unchecked", "unused", "cast"} )
public class CASTVisitor extends AbstractParseTreeVisitor<Object> implements IAgentVisitor, IPlanBundleVisitor
{

    /**
     * initial goal
     */
    protected ILiteral m_InitialGoal;
    /**
     * set with initial beliefs
     */
    protected final Set<ILiteral> m_InitialBeliefs = new HashSet<>();
    /**
     * map with plans
     */
    protected final SetMultimap<ITrigger<?>, IPlan> m_plans = HashMultimap.create();
    /**
     * map with action definition
     */
    protected final Map<CPath, IAction> m_actions;

    /**
     * ctor
     *
     * @param p_actions set with actions
     */
    public CASTVisitor( final Set<IAction> p_actions )
    {
        m_actions = p_actions.stream().collect( Collectors.toMap( IAction::getName, i -> i ) );
    }

    // ---------------------------------------------------------------------------------------------------------------------------------------------------------

    // --- agent rules -----------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public final Object visitAgent( final AgentParser.AgentContext p_context )
    {
        return this.visitChildren( p_context );
    }



    @Override
    public Object visitInitial_beliefs( final AgentParser.Initial_beliefsContext p_context )
    {
        p_context.belief().parallelStream().map( i -> (ILiteral) this.visitBelief( i ) ).forEach( m_InitialBeliefs::add );
        return null;
    }



    @Override
    public Object visitInitial_goal( final AgentParser.Initial_goalContext p_context )
    {
        m_InitialGoal = new CLiteral( p_context.atom().getText() );
        return null;
    }

    // ---------------------------------------------------------------------------------------------------------------------------------------------------------

    // --- plan bundle rules -----------------------------------------------------------------------------------------------------------------------------------

    @Override
    public Object visitPlanbundle( final PlanBundleParser.PlanbundleContext p_context )
    {
        return this.visitChildren( p_context );
    }

    // ---------------------------------------------------------------------------------------------------------------------------------------------------------

    // --- AgentSpeak(L) rules ---------------------------------------------------------------------------------------------------------------------------------

    @Override
    public Object visitBelief( final AgentParser.BeliefContext p_context )
    {
        return new CLiteral( (CLiteral) this.visitLiteral( p_context.literal() ), p_context.STRONGNEGATION() != null );
    }

    @Override
    public Object visitBelief( final PlanBundleParser.BeliefContext p_context )
    {
        return new CLiteral( (CLiteral) this.visitLiteral( p_context.literal() ), p_context.STRONGNEGATION() != null );
    }



    @Override
    public final Object visitPlans( final AgentParser.PlansContext p_context )
    {
        return this.visitChildren( p_context );
    }

    @Override
    public final Object visitPlans( final PlanBundleParser.PlansContext p_context )
    {
        return this.visitChildren( p_context );
    }



    @Override
    public final Object visitPrinciples( final AgentParser.PrinciplesContext p_context )
    {
        return this.visitChildren( p_context );
    }

    @Override
    public Object visitPrinciple( final AgentParser.PrincipleContext p_context )
    {
        return this.visitChildren( p_context );
    }



    @Override
    public Object visitPlan( final AgentParser.PlanContext p_context )
    {
        final ILiteral l_head = (ILiteral) this.visitLiteral( p_context.literal() );
        final ITrigger.EType l_trigger = (ITrigger.EType) this.visitPlan_trigger( p_context.plan_trigger() );
        final Set<IAnnotation<?>> l_annotation = (Set) this.visitAnnotations( p_context.annotations() );

        // parallel stream does not work with multi hashmap
        p_context.plandefinition().stream().forEach( i -> {

            final Pair<Object, List<IExecution>> l_content = (Pair<Object, List<IExecution>>) this.visitPlandefinition( i );
            final IPlan l_plan = new CPlan( new CTrigger( l_trigger, l_head.getFQNFunctor() ), l_head, l_content.getRight(), l_annotation );
            m_plans.put( l_plan.getTrigger(), l_plan );

        } );

        return null;
    }

    @Override
    public Object visitPlan( final PlanBundleParser.PlanContext p_context )
    {
        final ILiteral l_head = (ILiteral) this.visitLiteral( p_context.literal() );
        final ITrigger.EType l_trigger = (ITrigger.EType) this.visitPlan_trigger( p_context.plan_trigger() );
        final Set<IAnnotation<?>> l_annotation = (Set) this.visitAnnotations( p_context.annotations() );

        // parallel stream does not work with multi hashmap
        p_context.plandefinition().stream().forEach( i -> {

            final Pair<Object, List<IExecution>> l_content = (Pair<Object, List<IExecution>>) this.visitPlandefinition( i );
            final IPlan l_plan = new CPlan( new CTrigger( l_trigger, l_head.getFQNFunctor() ), l_head, l_content.getRight(), l_annotation );
            m_plans.put( l_plan.getTrigger(), l_plan );

        } );

        return null;
    }



    @Override
    public Object visitPlandefinition( final AgentParser.PlandefinitionContext p_context )
    {
        return new ImmutablePair<Object, List<IExecution>>(
                this.visitPlan_context( p_context.plan_context() ), (List<IExecution>) this.visitBody( p_context.body() ) );
    }

    @Override
    public Object visitPlandefinition( final PlanBundleParser.PlandefinitionContext p_context )
    {
        return new ImmutablePair<Object, List<IExecution>>(
                this.visitPlan_context( p_context.plan_context() ), (List<IExecution>) this.visitBody( p_context.body() ) );
    }



    @Override
    public Object visitAnnotations( final AgentParser.AnnotationsContext p_context )
    {
        if ( ( p_context == null ) || ( p_context.isEmpty() ) )
            return Collections.EMPTY_SET;

        final Set<IAnnotation<?>> l_annotation = new HashSet<>();
        if ( p_context.annotation_atom() != null )
            p_context.annotation_atom().stream().map( i -> (IAnnotation) this.visitAnnotation_atom( i ) ).forEach( l_annotation::add );
        if ( p_context.annotation_literal() != null )
            p_context.annotation_literal().stream().map( i -> (IAnnotation) this.visitAnnotation_literal( i ) ).forEach( l_annotation::add );

        return l_annotation.isEmpty() ? Collections.EMPTY_SET : l_annotation;
    }

    @Override
    public Object visitAnnotations( final PlanBundleParser.AnnotationsContext p_context )
    {
        if ( ( p_context == null ) || ( p_context.isEmpty() ) )
            return Collections.EMPTY_SET;

        final Set<IAnnotation<?>> l_annotation = new HashSet<>();
        if ( p_context.annotation_atom() != null )
            p_context.annotation_atom().stream().map( i -> (IAnnotation) this.visitAnnotation_atom( i ) ).forEach( l_annotation::add );
        if ( p_context.annotation_literal() != null )
            p_context.annotation_literal().stream().map( i -> (IAnnotation) this.visitAnnotation_literal( i ) ).forEach( l_annotation::add );

        return l_annotation.isEmpty() ? Collections.EMPTY_SET : l_annotation;
    }



    @Override
    public Object visitAnnotation_atom( final AgentParser.Annotation_atomContext p_context )
    {
        if ( p_context.ATOMIC() != null )
            return new CAtomAnnotation( IAnnotation.EType.ATOMIC );

        if ( p_context.EXCLUSIVE() != null )
            return new CAtomAnnotation( IAnnotation.EType.EXCLUSIVE );

        if ( p_context.PARALLEL() != null )
            return new CAtomAnnotation( IAnnotation.EType.PARALLEL );

        throw new CIllegalArgumentException( CCommon.getLanguageString( this, "atomannotation", p_context.getText() ) );
    }

    @Override
    public Object visitAnnotation_atom( final PlanBundleParser.Annotation_atomContext p_context )
    {
        if ( p_context.ATOMIC() != null )
            return new CAtomAnnotation( IAnnotation.EType.ATOMIC );

        if ( p_context.EXCLUSIVE() != null )
            return new CAtomAnnotation( IAnnotation.EType.EXCLUSIVE );

        if ( p_context.PARALLEL() != null )
            return new CAtomAnnotation( IAnnotation.EType.PARALLEL );

        throw new CIllegalArgumentException( CCommon.getLanguageString( this, "atomannotation", p_context.getText() ) );
    }



    @Override
    public final Object visitAnnotation_literal( final AgentParser.Annotation_literalContext p_context )
    {
        return this.visitChildren( p_context );
    }

    @Override
    public final Object visitAnnotation_literal( final PlanBundleParser.Annotation_literalContext p_context )
    {
        return this.visitChildren( p_context );
    }



    @Override
    public Object visitAnnotation_numeric_literal( final AgentParser.Annotation_numeric_literalContext p_context )
    {
        if ( p_context.FUZZY() != null )
            return new CNumberAnnotation<>( IAnnotation.EType.FUZZY, (Number) this.visitNumber( p_context.number() ) );

        if ( p_context.SCORE() != null )
            return new CNumberAnnotation<>( IAnnotation.EType.SCORE, ( (Number) this.visitNumber( p_context.number() ) ).doubleValue() );

        throw new CIllegalArgumentException( CCommon.getLanguageString( this, "numberannotation", p_context.getText() ) );
    }

    @Override
    public Object visitAnnotation_numeric_literal( final PlanBundleParser.Annotation_numeric_literalContext p_context )
    {
        if ( p_context.FUZZY() != null )
            return new CNumberAnnotation<>( IAnnotation.EType.FUZZY, (Number) this.visitNumber( p_context.number() ) );

        if ( p_context.SCORE() != null )
            return new CNumberAnnotation<>( IAnnotation.EType.SCORE, ( (Number) this.visitNumber( p_context.number() ) ).longValue() );

        throw new CIllegalArgumentException( CCommon.getLanguageString( this, "numberannotation", p_context.getText() ) );
    }



    @Override
    public Object visitAnnotation_symbolic_literal( final AgentParser.Annotation_symbolic_literalContext p_context )
    {
        if ( p_context.EXPIRES() != null )
            return new CSymbolicAnnotation( IAnnotation.EType.EXPIRES, (ILiteral) this.visitAtom( p_context.atom() ) );

        throw new CIllegalArgumentException( CCommon.getLanguageString( this, "symbolicliteralannotation", p_context.getText() ) );
    }

    @Override
    public Object visitAnnotation_symbolic_literal( final PlanBundleParser.Annotation_symbolic_literalContext p_context )
    {
        if ( p_context.EXPIRES() != null )
            return new CSymbolicAnnotation( IAnnotation.EType.EXPIRES, (ILiteral) this.visitAtom( p_context.atom() ) );

        throw new CIllegalArgumentException( CCommon.getLanguageString( this, "symbolicliteralannotation", p_context.getText() ) );
    }



    @Override
    public final Object visitPlan_trigger( final AgentParser.Plan_triggerContext p_context )
    {
        return this.visitChildren( p_context );
    }

    @Override
    public final Object visitPlan_trigger( final PlanBundleParser.Plan_triggerContext p_context )
    {
        return this.visitChildren( p_context );
    }



    @Override
    public Object visitPlan_goal_trigger( final AgentParser.Plan_goal_triggerContext p_context )
    {
        switch ( p_context.getText() )
        {
            case "+!":
                return ITrigger.EType.ADDGOAL;
            case "-!":
                return ITrigger.EType.DELETEGOAL;

            default:
                throw new CIllegalArgumentException( CCommon.getLanguageString( this, "goaltrigger", p_context.getText() ) );
        }
    }

    @Override
    public Object visitPlan_goal_trigger( final PlanBundleParser.Plan_goal_triggerContext p_context )
    {
        switch ( p_context.getText() )
        {
            case "+!":
                return ITrigger.EType.ADDGOAL;
            case "-!":
                return ITrigger.EType.DELETEGOAL;

            default:
                throw new CIllegalArgumentException( CCommon.getLanguageString( this, "goaltrigger", p_context.getText() ) );
        }
    }



    @Override
    public Object visitPlan_belief_trigger( final AgentParser.Plan_belief_triggerContext p_context )
    {
        switch ( p_context.getText() )
        {
            case "+":
                return ITrigger.EType.ADDBELIEF;
            case "-":
                return ITrigger.EType.DELETEBELIEF;
            case "-+":
                return ITrigger.EType.CHANGEBELIEF;

            default:
                throw new CIllegalArgumentException( CCommon.getLanguageString( this, "belieftrigger", p_context.getText() ) );
        }
    }

    @Override
    public Object visitPlan_belief_trigger( final PlanBundleParser.Plan_belief_triggerContext p_context )
    {
        switch ( p_context.getText() )
        {
            case "+":
                return ITrigger.EType.ADDBELIEF;
            case "-":
                return ITrigger.EType.DELETEBELIEF;
            case "-+":
                return ITrigger.EType.CHANGEBELIEF;

            default:
                throw new CIllegalArgumentException( CCommon.getLanguageString( this, "belieftrigger", p_context.getText() ) );
        }
    }



    @Override
    public Object visitPlan_context( final AgentParser.Plan_contextContext p_context )
    {
        return p_context == null ? "" : p_context.getText();
    }

    @Override
    public Object visitPlan_context( final PlanBundleParser.Plan_contextContext p_context )
    {
        return p_context == null ? "" : p_context.getText();
    }



    @Override
    public Object visitBody( final AgentParser.BodyContext p_context )
    {
        // filter null values of the body formular, because blank lines add a null value
        return p_context.body_formula().parallelStream().filter( i -> i != null ).map( i -> {

            final Object l_item = this.visitBody_formula( i );

            // body actions directly return
            if ( l_item instanceof IExecution )
                return l_item;

            // literals are actions
            if ( l_item instanceof ILiteral )
            {
                final ILiteral l_literal = (ILiteral) l_item;
                return new CProxyAction( m_actions, l_literal );
            }
                /*
            {
                final IExecution l_action = m_actions.get( ( (ILiteral) l_item ).getFQNFunctor() );
                if ( l_action == null )
                    throw new CIllegalArgumentException( CCommon.getLanguageString( this, "actionunknown", l_item ) );

                return l_action;
            }
            */

            // otherwise only simple types encapsulate
            return new CRawAction<>( l_item );

        } ).collect( Collectors.toList() );
    }

    @Override
    public Object visitBody( final PlanBundleParser.BodyContext p_context )
    {
        // filter null values of the body formular, because blank lines add a null value
        return p_context.body_formula().parallelStream().filter( i -> i != null ).map( i -> {

            final Object l_item = this.visitBody_formula( i );

            // body actions directly return
            if ( l_item instanceof IExecution )
                return l_item;

            // literals are actions
            if ( l_item instanceof ILiteral )
            {
                final ILiteral l_literal = (ILiteral) l_item;
                return new CProxyAction( m_actions, l_literal );
            }
                /*
            {
                final IExecution l_action = m_actions.get( ( (ILiteral) l_item ).getFQNFunctor() );
                if ( l_action == null )
                    throw new CIllegalArgumentException( CCommon.getLanguageString( this, "actionunknown", l_item ) );

                return l_action;
            }
            */

            // otherwise only simple types encapsulate
            return new CRawAction<>( l_item );

        } ).collect( Collectors.toList() );
    }



    @Override
    public final Object visitBody_formula( final AgentParser.Body_formulaContext p_context )
    {
        return this.visitChildren( p_context );
    }

    @Override
    public final Object visitBody_formula( final PlanBundleParser.Body_formulaContext p_context )
    {
        return this.visitChildren( p_context );
    }



    @Override
    public final Object visitPrinciples( final PlanBundleParser.PrinciplesContext p_context )
    {
        return this.visitChildren( p_context );
    }

    @Override
    public Object visitPrinciple( final PlanBundleParser.PrincipleContext p_context )
    {
        return this.visitChildren( p_context );
    }



    @Override
    public final Object visitBlock_formula( final AgentParser.Block_formulaContext p_context )
    {
        return this.visitChildren( p_context );
    }

    @Override
    public final Object visitBlock_formula( final PlanBundleParser.Block_formulaContext p_context )
    {
        return this.visitChildren( p_context );
    }



    @Override
    public Object visitIf_else( final AgentParser.If_elseContext p_context )
    {
        return this.visitChildren( p_context );
    }

    @Override
    public Object visitIf_else( final PlanBundleParser.If_elseContext p_context )
    {
        return this.visitChildren( p_context );
    }



    @Override
    public Object visitWhile_loop( final AgentParser.While_loopContext p_context )
    {
        return this.visitChildren( p_context );
    }

    @Override
    public Object visitWhile_loop( final PlanBundleParser.While_loopContext p_context )
    {
        return this.visitChildren( p_context );
    }



    @Override
    public Object visitFor_loop( final AgentParser.For_loopContext p_context )
    {
        return this.visitChildren( p_context );
    }

    @Override
    public Object visitFor_loop( final PlanBundleParser.For_loopContext p_context )
    {
        return this.visitChildren( p_context );
    }



    @Override
    public Object visitForeach_loop( final AgentParser.Foreach_loopContext p_context )
    {
        return this.visitChildren( p_context );
    }

    @Override
    public Object visitForeach_loop( final PlanBundleParser.Foreach_loopContext p_context )
    {
        return this.visitChildren( p_context );
    }



    @Override
    public Object visitLogical_expression( final AgentParser.Logical_expressionContext p_context )
    {
        return this.visitChildren( p_context );
    }

    @Override
    public Object visitLogical_expression( final PlanBundleParser.Logical_expressionContext p_context )
    {
        return this.visitChildren( p_context );
    }



    @Override
    public Object visitComparison_expression( final AgentParser.Comparison_expressionContext p_context )
    {
        return this.visitChildren( p_context );
    }

    @Override
    public Object visitComparison_expression( final PlanBundleParser.Comparison_expressionContext p_context )
    {
        return this.visitChildren( p_context );
    }



    @Override
    public Object visitArithmetic_expression( final AgentParser.Arithmetic_expressionContext p_context )
    {
        return this.visitChildren( p_context );
    }

    @Override
    public Object visitArithmetic_expression( final PlanBundleParser.Arithmetic_expressionContext p_context )
    {
        return this.visitChildren( p_context );
    }



    @Override
    public Object visitAssignment_expression( final AgentParser.Assignment_expressionContext p_context )
    {
        return this.visitChildren( p_context );
    }

    @Override
    public Object visitAssignment_expression( final PlanBundleParser.Assignment_expressionContext p_context )
    {
        return this.visitChildren( p_context );
    }



    @Override
    public Object visitUnary_expression( final AgentParser.Unary_expressionContext p_context )
    {
        switch ( p_context.unaryoperator().getText() )
        {
            case "++":
                return new CIncrement<>( (IVariable) this.visitVariable( p_context.variable() ) );

            case "--":
                return new CDecrement<>( (IVariable) this.visitVariable( p_context.variable() ) );

            default:
                throw new CIllegalArgumentException( CCommon.getLanguageString( this, "unaryoperator", p_context.getText() ) );
        }
    }

    @Override
    public Object visitUnary_expression( final PlanBundleParser.Unary_expressionContext p_context )
    {
        switch ( p_context.unaryoperator().getText() )
        {
            case "++":
                return new CIncrement<>( (IVariable) this.visitVariable( p_context.variable() ) );

            case "--":
                return new CDecrement<>( (IVariable) this.visitVariable( p_context.variable() ) );

            default:
                throw new CIllegalArgumentException( CCommon.getLanguageString( this, "unaryoperator", p_context.getText() ) );
        }
    }



    @Override
    public Object visitAchievement_goal_action( final AgentParser.Achievement_goal_actionContext p_context )
    {
        return new CAchievementGoal( (ILiteral) this.visitLiteral( p_context.literal() ), p_context.DOUBLEEXCLAMATIONMARK() != null );
    }

    @Override
    public Object visitAchievement_goal_action( final PlanBundleParser.Achievement_goal_actionContext p_context )
    {
        return new CAchievementGoal( (ILiteral) this.visitLiteral( p_context.literal() ), p_context.DOUBLEEXCLAMATIONMARK() != null );
    }



    @Override
    public Object visitTest_goal_action( final AgentParser.Test_goal_actionContext p_context )
    {
        return new CTestGoal( (ILiteral) this.visitLiteral( p_context.literal() ) );
    }

    @Override
    public Object visitTest_goal_action( final PlanBundleParser.Test_goal_actionContext p_context )
    {
        return new CTestGoal( (ILiteral) this.visitLiteral( p_context.literal() ) );
    }



    @Override
    public Object visitBelief_action( final AgentParser.Belief_actionContext p_context )
    {
        if ( p_context.PLUS() != null )
            return new CBeliefAction( (ILiteral) this.visitLiteral( p_context.literal() ), CBeliefAction.EAction.Add );

        if ( p_context.MINUS() != null )
            return new CBeliefAction( (ILiteral) this.visitLiteral( p_context.literal() ), CBeliefAction.EAction.Delete );

        if ( p_context.MINUSPLUS() != null )
            return new CBeliefAction( (ILiteral) this.visitLiteral( p_context.literal() ), CBeliefAction.EAction.Change );

        throw new CIllegalArgumentException( CCommon.getLanguageString( this, "beliefaction", p_context.getText() ) );
    }

    @Override
    public Object visitBelief_action( final PlanBundleParser.Belief_actionContext p_context )
    {
        if ( p_context.PLUS() != null )
            return new CBeliefAction( (ILiteral) this.visitLiteral( p_context.literal() ), CBeliefAction.EAction.Add );

        if ( p_context.MINUS() != null )
            return new CBeliefAction( (ILiteral) this.visitLiteral( p_context.literal() ), CBeliefAction.EAction.Delete );

        if ( p_context.MINUSPLUS() != null )
            return new CBeliefAction( (ILiteral) this.visitLiteral( p_context.literal() ), CBeliefAction.EAction.Change );

        throw new CIllegalArgumentException( CCommon.getLanguageString( this, "beliefaction", p_context.getText() ) );
    }



    @Override
    public Object visitDeconstruct_expression( final AgentParser.Deconstruct_expressionContext p_context )
    {
        return this.visitChildren( p_context );
    }

    @Override
    public Object visitDeconstruct_expression( final PlanBundleParser.Deconstruct_expressionContext p_context )
    {
        return this.visitChildren( p_context );
    }

    // ---------------------------------------------------------------------------------------------------------------------------------------------------------


    // --- simple datatypes ------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public Object visitLiteral( final AgentParser.LiteralContext p_context )
    {
        return new CLiteral(
                p_context.AT() != null,
                this.visitAtom( p_context.atom() ).toString(),
                (Collection<ITerm>) this.visitTermlist( p_context.termlist() ),
                (Collection<ILiteral>) this.visitLiteralset( p_context.literalset() )
        );
    }

    @Override
    public Object visitLiteral( final PlanBundleParser.LiteralContext p_context )
    {
        return new CLiteral(
                p_context.AT() != null,
                this.visitAtom( p_context.atom() ).toString(),
                (Collection<ITerm>) this.visitTermlist( p_context.termlist() ),
                (Collection<ILiteral>) this.visitLiteralset( p_context.literalset() )
        );
    }



    @Override
    public Object visitLiteralset( final AgentParser.LiteralsetContext p_context )
    {
        if ( ( p_context == null ) || ( p_context.isEmpty() ) )
            return Collections.EMPTY_LIST;

        return p_context.literal().stream().map( i -> this.visitLiteral( i ) ).filter( i -> i != null ).collect( Collectors.toList() );
    }

    @Override
    public Object visitLiteralset( final PlanBundleParser.LiteralsetContext p_context )
    {
        if ( ( p_context == null ) || ( p_context.isEmpty() ) )
            return Collections.EMPTY_LIST;

        return p_context.literal().stream().map( i -> this.visitLiteral( i ) ).filter( i -> i != null ).collect( Collectors.toList() );
    }



    @Override
    public Object visitTerm( final AgentParser.TermContext p_context )
    {
        if ( p_context.string() != null )
            return this.visitString( p_context.string() );
        if ( p_context.number() != null )
            return this.visitNumber( p_context.number() );
        if ( p_context.literal() != null )
            return this.visitLiteral( p_context.literal() );
        if ( p_context.variable() != null )
            return this.visitVariable( p_context.variable() );
        if ( p_context.arithmetic_expression() != null )
            return this.visitArithmetic_expression( p_context.arithmetic_expression() );
        if ( p_context.logical_expression() != null )
            return this.visitLogical_expression( p_context.logical_expression() );
        if ( p_context.termlist() != null )
            return this.visitTermlist( p_context.termlist() );

        throw new CIllegalArgumentException( CCommon.getLanguageString( this, "termunknown", p_context.getText() ) );
    }

    @Override
    public Object visitTerm( final PlanBundleParser.TermContext p_context )
    {
        if ( p_context.string() != null )
            return this.visitString( p_context.string() );
        if ( p_context.number() != null )
            return this.visitNumber( p_context.number() );
        if ( p_context.literal() != null )
            return this.visitLiteral( p_context.literal() );
        if ( p_context.variable() != null )
            return this.visitVariable( p_context.variable() );
        if ( p_context.arithmetic_expression() != null )
            return this.visitArithmetic_expression( p_context.arithmetic_expression() );
        if ( p_context.logical_expression() != null )
            return this.visitLogical_expression( p_context.logical_expression() );
        if ( p_context.termlist() != null )
            return this.visitTermlist( p_context.termlist() );

        throw new CIllegalArgumentException( CCommon.getLanguageString( this, "termunknown", p_context.getText() ) );
    }



    @Override
    public Object visitTermlist( final AgentParser.TermlistContext p_context )
    {
        if ( ( p_context == null ) || ( p_context.isEmpty() ) )
            return Collections.EMPTY_LIST;

        return p_context.term().stream().map( i -> this.visitTerm( i ) ).filter( i -> i != null ).map(
                i -> i instanceof ITerm ? (ITerm) i : new CRawTerm<>( i )
        ).collect( Collectors.toList() );
    }

    @Override
    public Object visitTermlist( final PlanBundleParser.TermlistContext p_context )
    {
        if ( ( p_context == null ) || ( p_context.isEmpty() ) )
            return Collections.EMPTY_LIST;

        return p_context.term().stream().map( i -> this.visitTerm( i ) ).filter( i -> i != null ).map(
                i -> i instanceof ITerm ? (ITerm) i : new CRawTerm<>( i )
        ).collect( Collectors.toList() );
    }



    @Override
    public Object visitVariablelist( final AgentParser.VariablelistContext p_context )
    {
        return this.visitChildren( p_context );
    }

    @Override
    public Object visitVariablelist( final PlanBundleParser.VariablelistContext p_context )
    {
        return this.visitChildren( p_context );
    }

    // ---------------------------------------------------------------------------------------------------------------------------------------------------------


    // --- raw rules -------------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public final Object visitNumber( final AgentParser.NumberContext p_context )
    {
        return this.visitChildren( p_context );
    }

    @Override
    public Object visitNumber( final PlanBundleParser.NumberContext p_context )
    {
        return this.visitChildren( p_context );
    }



    @Override
    public final Object visitIntegernumber( final AgentParser.IntegernumberContext p_context )
    {
        return Long.valueOf( p_context.getText() );
    }

    @Override
    public Object visitIntegernumber( final PlanBundleParser.IntegernumberContext p_context )
    {
        return Long.valueOf( p_context.getText() );
    }



    @Override
    public Object visitFloatnumber( final AgentParser.FloatnumberContext p_context )
    {
        switch ( p_context.getText() )
        {
            case "pi":
                return ( p_context.MINUS() == null ? 1 : -1 ) * Math.PI;
            case "euler":
                return ( p_context.MINUS() == null ? 1 : -1 ) * Math.E;
            case "lightspeed":
                return (double) ( ( p_context.MINUS() == null ? 1 : -1 ) * 299792458 );
            case "avogadro":
                return ( p_context.MINUS() == null ? 1 : -1 ) * 6.0221412927e23;
            case "boltzmann":
                return ( p_context.MINUS() == null ? 1 : -1 ) * 8.617330350e-15;
            case "gravity":
                return ( p_context.MINUS() == null ? 1 : -1 ) * 6.67408e-11;
            case "electron":
                return ( p_context.MINUS() == null ? 1 : -1 ) * 9.10938356e-31;
            case "neutron":
                return ( p_context.MINUS() == null ? 1 : -1 ) * 1674927471214e-27;
            case "proton":
                return ( p_context.MINUS() == null ? 1 : -1 ) * 1.6726219e-27;
            case "infinity":
                return p_context.MINUS() == null ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;

            default:
                return Double.valueOf( p_context.getText() );
        }
    }

    @Override
    public Object visitFloatnumber( final PlanBundleParser.FloatnumberContext p_context )
    {
        switch ( p_context.getText() )
        {
            case "pi":
                return ( p_context.MINUS() == null ? 1 : -1 ) * Math.PI;
            case "euler":
                return ( p_context.MINUS() == null ? 1 : -1 ) * Math.E;
            case "lightspeed":
                return (double) ( ( p_context.MINUS() == null ? 1 : -1 ) * 299792458 );
            case "avogadro":
                return ( p_context.MINUS() == null ? 1 : -1 ) * 6.0221412927e23;
            case "boltzmann":
                return ( p_context.MINUS() == null ? 1 : -1 ) * 8.617330350e-15;
            case "gravity":
                return ( p_context.MINUS() == null ? 1 : -1 ) * 6.67408e-11;
            case "electron":
                return ( p_context.MINUS() == null ? 1 : -1 ) * 9.10938356e-31;
            case "neutron":
                return ( p_context.MINUS() == null ? 1 : -1 ) * 1674927471214e-27;
            case "proton":
                return ( p_context.MINUS() == null ? 1 : -1 ) * 1.6726219e-27;
            case "infinity":
                return p_context.MINUS() == null ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;

            default:
                return Double.valueOf( p_context.getText() );
        }
    }



    @Override
    public final Object visitLogicalvalue( final AgentParser.LogicalvalueContext p_context )
    {
        return p_context.TRUE() != null ? true : false;
    }

    @Override
    public Object visitLogicalvalue( final PlanBundleParser.LogicalvalueContext p_context )
    {
        return p_context.TRUE() != null ? true : false;
    }



    @Override
    public final Object visitConstant( final AgentParser.ConstantContext p_context )
    {
        return this.visitChildren( p_context );
    }

    @Override
    public Object visitConstant( final PlanBundleParser.ConstantContext p_context )
    {
        return this.visitChildren( p_context );
    }



    @Override
    public final Object visitString( final AgentParser.StringContext p_context )
    {
        return p_context.getText();
    }

    @Override
    public Object visitString( final PlanBundleParser.StringContext p_context )
    {
        return p_context.getText();
    }



    @Override
    public Object visitAtom( final AgentParser.AtomContext p_context )
    {
        return p_context.getText();
    }

    @Override
    public Object visitAtom( final PlanBundleParser.AtomContext p_context )
    {
        return p_context.getText();
    }



    @Override
    public Object visitVariable( final AgentParser.VariableContext p_context )
    {
        return p_context.AT() == null ? new CVariable<>( p_context.getText() ) : new CMutexVariable<>( p_context.getText() );
    }

    @Override
    public Object visitVariable( final PlanBundleParser.VariableContext p_context )
    {
        return p_context.AT() == null ? new CVariable<>( p_context.getText() ) : new CMutexVariable<>( p_context.getText() );
    }



    @Override
    public final Object visitComparator( final AgentParser.ComparatorContext p_context )
    {
        return this.visitChildren( p_context );
    }

    @Override
    public Object visitComparator( final PlanBundleParser.ComparatorContext p_context )
    {
        return this.visitChildren( p_context );
    }



    @Override
    public final Object visitArithmeticoperator( final AgentParser.ArithmeticoperatorContext p_context )
    {
        return this.visitChildren( p_context );
    }

    @Override
    public Object visitArithmeticoperator( final PlanBundleParser.ArithmeticoperatorContext p_context )
    {
        return this.visitChildren( p_context );
    }



    @Override
    public final Object visitUnaryoperator( final AgentParser.UnaryoperatorContext p_context )
    {
        return this.visitChildren( p_context );
    }

    @Override
    public Object visitUnaryoperator( final PlanBundleParser.UnaryoperatorContext p_context )
    {
        return this.visitChildren( p_context );
    }



    @Override
    public Object visitBinaryoperator( final AgentParser.BinaryoperatorContext p_context )
    {
        return this.visitChildren( p_context );
    }

    @Override
    public Object visitBinaryoperator( final PlanBundleParser.BinaryoperatorContext p_context )
    {
        return this.visitChildren( p_context );
    }

    // ---------------------------------------------------------------------------------------------------------------------------------------------------------


    // --- getter structure ------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public final Set<ILiteral> getInitialBeliefs()
    {
        return m_InitialBeliefs;
    }



    @Override
    public final SetMultimap<ITrigger<?>, IPlan> getPlans()
    {
        return m_plans;
    }



    @Override
    public final Map<String, Object> getRules()
    {
        return null;
    }



    @Override
    public final ILiteral getInitialGoal()
    {
        return m_InitialGoal;
    }

    // ---------------------------------------------------------------------------------------------------------------------------------------------------------

}