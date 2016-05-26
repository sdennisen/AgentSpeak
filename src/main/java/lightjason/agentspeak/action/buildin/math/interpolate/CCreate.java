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

package lightjason.agentspeak.action.buildin.math.interpolate;

import lightjason.agentspeak.action.buildin.IBuildinAction;
import lightjason.agentspeak.error.CIllegalStateException;
import lightjason.agentspeak.language.CCommon;
import lightjason.agentspeak.language.CRawTerm;
import lightjason.agentspeak.language.ITerm;
import lightjason.agentspeak.language.execution.IContext;
import lightjason.agentspeak.language.execution.fuzzy.CFuzzyValue;
import lightjason.agentspeak.language.execution.fuzzy.IFuzzyValue;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.AkimaSplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.DividedDifferenceInterpolator;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.LoessInterpolator;
import org.apache.commons.math3.analysis.interpolation.NevilleInterpolator;

import java.util.List;


/**
 * action to create a spline interpolation
 */
public final class CCreate extends IBuildinAction
{

    /**
     * ctor
     */
    public CCreate()
    {
        super( 3 );
    }

    @Override
    public final int getMinimalArgumentNumber()
    {
        return 3;
    }

    @Override
    public final IFuzzyValue<Boolean> execute( final IContext p_context, final boolean p_parallel, final List<ITerm> p_argument, final List<ITerm> p_return,
                                               final List<ITerm> p_annotation
    )
    {
        p_return.add( CRawTerm.from(
            EType.valueOf( CCommon.<String, ITerm>getRawValue( p_argument.get( 0 ) ).trim().toUpperCase() ).get(
                CCommon.flatList( CCommon.getRawValue( p_argument.get( 1 ) ) ).stream()
                       .mapToDouble( i -> CCommon.<Number, ITerm>getRawValue( i ).doubleValue() ).toArray(),
                CCommon.flatList( CCommon.getRawValue( p_argument.get( 2 ) ) ).stream()
                       .mapToDouble( i -> CCommon.<Number, ITerm>getRawValue( i ).doubleValue() ).toArray()
            )
        ) );

        return CFuzzyValue.from( true );
    }


    /**
     * enum type of interpolating definitions
     */
    private enum EType
    {
        AKIMA,
        DIVIDEDDIFFERENCE,
        LINEAR,
        LOESS,
        NEVILLE;


        /**
         * returns the interpolate objext
         *
         * @param p_xvalues x-values
         * @param p_yvalues y-values
         * @return interpolate object
         */
        public final UnivariateFunction get( final double[] p_xvalues, final double[] p_yvalues )
        {
            switch ( this )
            {
                case AKIMA:
                    return new AkimaSplineInterpolator().interpolate( p_xvalues, p_yvalues );

                case DIVIDEDDIFFERENCE:
                    return new DividedDifferenceInterpolator().interpolate( p_xvalues, p_yvalues );

                case LINEAR:
                    return new LinearInterpolator().interpolate( p_xvalues, p_yvalues );

                case LOESS:
                    return new LoessInterpolator().interpolate( p_xvalues, p_yvalues );

                case NEVILLE:
                    return new NevilleInterpolator().interpolate( p_xvalues, p_yvalues );

                default:
                    throw new CIllegalStateException( lightjason.agentspeak.common.CCommon.getLanguageString( this, "unknown", this ) );
            }
        }
    }

}