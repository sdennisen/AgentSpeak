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

package lightjason.language.variable;

import lightjason.language.ILiteral;
import lightjason.language.ITerm;
import lightjason.language.execution.IContext;

import java.util.stream.Stream;


/**
 *
 */
public interface IVariableEvaluate extends ITerm
{

    /**
     * returns mutex flag
     *
     * @return mutex flag
     */
    boolean hasMutex();

    /**
     * evaluates the content of the variable
     *
     * @param p_context execution context
     * @return unified literal
     */
    ILiteral evaluate( final IContext p_context );


    /**
     * returns a stream with all used variables
     *
     * @return variable stream (variables will be cloned on instantiation)
     */
    Stream<IVariable<?>> getVariables();

}
