/*
 * This file is part of fabric-loom, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2016-2019 FabricMC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.fabricmc.loom.util;

import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.tinyremapper.IMappingProvider;

public class TinyRemapperMappingsHelper {
	private TinyRemapperMappingsHelper() { }

	private static IMappingProvider.Member memberOf(String className, String memberName, String descriptor) {
		return new IMappingProvider.Member(className, memberName, descriptor);
	}

	public static IMappingProvider create(MappingTree mappings, String from, String to, boolean remapLocalVariables) {
		return (acceptor) -> {
			for (var classDef : mappings.getClasses()) {
				String className = classDef.getName(from);
				acceptor.acceptClass(className, classDef.getName(to));

				for (var field : classDef.getFields()) {
					acceptor.acceptField(memberOf(className, field.getName(from), field.getDesc(from)), field.getName(to));
				}

				for (var method : classDef.getMethods()) {
					IMappingProvider.Member methodIdentifier = memberOf(className, method.getName(from), method.getDesc(from));
					acceptor.acceptMethod(methodIdentifier, method.getName(to));

					if (remapLocalVariables) {
						for (var parameter : method.getArgs()) {
							acceptor.acceptMethodArg(methodIdentifier, parameter.getLvIndex(), parameter.getName(to));
						}

						for (var localVariable : method.getVars()) {
							acceptor.acceptMethodVar(methodIdentifier, localVariable.getLvIndex(),
											localVariable.getStartOpIdx(), localVariable.getLvtRowIndex(),
											localVariable.getName(to));
						}
					}
				}
			}
		};
	}
}