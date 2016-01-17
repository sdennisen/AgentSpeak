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

package lightjason.agent.action.buildin.math.blas.vector;

import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.SparseDoubleMatrix1D;
import lightjason.agent.action.buildin.IBuildinAction;
import lightjason.language.CCommon;
import lightjason.language.CRawTerm;
import lightjason.language.ITerm;
import lightjason.language.execution.IContext;
import lightjason.language.execution.fuzzy.CBoolean;
import lightjason.language.execution.fuzzy.IFuzzyValue;

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
    public final IFuzzyValue<Boolean> execute( final IContext<?> p_context, final List<ITerm> p_annotation, final List<ITerm> p_argument,
                                               final List<ITerm> p_return
    )
    {
        // first argument is the element size,
        // optional second argument is matrix type (default dense-matrix)
        final List<ITerm> l_argument = CCommon.replaceVariableFromContext( p_context, p_argument );

        switch ( l_argument.size() > 1 ? EType.valueOf( CCommon.getRawValue( l_argument.get( 1 ) ) ) : EType.DENSE )
        {
            case DENSE:
                p_return.add(
                        new CRawTerm<>( new DenseDoubleMatrix1D( CCommon.getRawValue( l_argument.get( 0 ) ) ) )
                );
                break;

            case SPARSE:
                p_return.add(
                        new CRawTerm<>( new SparseDoubleMatrix1D( CCommon.getRawValue( l_argument.get( 0 ) ) ) )
                );
                break;

            default:
        }

        return CBoolean.from( true );
    }


    /**
     * matrix type
     */
    private enum EType
    {
        SPARSE,
        DENSE;
    }
}