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

package org.lightjason.agentspeak.action.buildin.math.blas.matrix;

import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import org.lightjason.agentspeak.action.buildin.IBuildinAction;
import org.lightjason.agentspeak.action.buildin.math.blas.EType;
import org.lightjason.agentspeak.language.CCommon;
import org.lightjason.agentspeak.language.CRawTerm;
import org.lightjason.agentspeak.language.ITerm;
import org.lightjason.agentspeak.language.execution.IContext;
import org.lightjason.agentspeak.language.execution.fuzzy.CFuzzyValue;
import org.lightjason.agentspeak.language.execution.fuzzy.IFuzzyValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * creates a dense- or sparse-matrix from a string
 * semicolon splits the rows, spaces / comma splits the columns
 */
public final class CParse extends IBuildinAction
{
    /**
     * ctor
     */
    public CParse()
    {
        super( 4 );
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
        switch ( p_argument.size() > 1 ? EType.valueOf( CCommon.raw( p_argument.get( 1 ) ) ) : EType.DENSE )
        {
            case DENSE:
                p_return.add(
                    CRawTerm.from( new DenseDoubleMatrix2D( parse( CCommon.raw( p_argument.get( 0 ) ) ) ) )
                );
                break;

            case SPARSE:
                p_return.add(
                    CRawTerm.from( new SparseDoubleMatrix2D( parse( CCommon.raw( p_argument.get( 0 ) ) ) ) )
                );
                break;

            default:
        }

        return CFuzzyValue.from( true );
    }

    /**
     * parse the string in a list of lists with doubles
     *
     * @param p_string string
     * @return 2D double array
     */
    private static double[][] parse( final String p_string )
    {
        final String[] l_rows = p_string.split( ";" );
        final List<List<Double>> l_matrix = new ArrayList<>();

        final double[][] l_return = new double[l_rows.length][
            Arrays.stream( l_rows )
                  .map( i -> Arrays.stream( i.trim().split( ",|\\s" ) )
                                   .map( String::trim )
                                   .filter( j -> !j.isEmpty() )
                                   .mapToDouble( Double::parseDouble )
                                   .boxed()
                                   .collect( Collectors.toList() )
                  )
                  .mapToInt( i -> {
                      l_matrix.add( i );
                      return i.size();
                  } )
                  .max()
                  .getAsInt()
            ];

        IntStream.range( 0, l_return.length )
                 .boxed()
                 .forEach( i -> IntStream.range( 0, l_return[i].length )
                                         .boxed()
                                         .forEach( j -> l_return[i][j] = l_matrix.get( i ).get( j ) ) );

        return l_return;
    }

}
