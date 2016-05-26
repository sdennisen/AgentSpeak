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

package lightjason.agentspeak.action.buildin.math.blas.vector;

import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.SparseDoubleMatrix1D;
import lightjason.agentspeak.action.buildin.IBuildinAction;
import lightjason.agentspeak.action.buildin.math.blas.EType;
import lightjason.agentspeak.language.CCommon;
import lightjason.agentspeak.language.CRawTerm;
import lightjason.agentspeak.language.ITerm;
import lightjason.agentspeak.language.execution.IContext;
import lightjason.agentspeak.language.execution.fuzzy.CFuzzyValue;
import lightjason.agentspeak.language.execution.fuzzy.IFuzzyValue;

import java.util.List;


/**
 * creates a dense- or sparse-vector
 */
public final class CCreate extends IBuildinAction
{
    /**
     * ctor
     */
    public CCreate()
    {
        super( 4 );
    }

    @Override
    public final int getMinimalArgumentNumber()
    {
        return 1;
    }

    @Override
    public final IFuzzyValue<Boolean> execute( final IContext p_context, final boolean p_parallel, final List<ITerm> p_argument, final List<ITerm> p_return,
                                               final List<ITerm> p_annotation
    )
    {
        // first argument is the element size,
        // optional second argument is matrix type (default dense-matrix)
        switch ( p_argument.size() > 1 ? EType.valueOf( CCommon.getRawValue( p_argument.get( 1 ) ) ) : EType.DENSE )
        {
            case DENSE:
                p_return.add(
                    CRawTerm.from( new DenseDoubleMatrix1D( CCommon.<Number, ITerm>getRawValue( p_argument.get( 0 ) ).intValue() ) )
                );
                break;

            case SPARSE:
                p_return.add(
                    CRawTerm.from( new SparseDoubleMatrix1D( CCommon.<Number, ITerm>getRawValue( p_argument.get( 0 ) ).intValue() ) )
                );
                break;

            default:
        }
        return CFuzzyValue.from( true );
    }

}