/*
 * This file is part of fabric-loom, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2019-2021 FabricMC
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

package net.fabricmc.loom.decompilers.fernflower;

import io.github.nickacpt.lightcraft.gradle.ConstantsKt;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fernflower.api.IFabricJavadocProvider;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructField;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.jetbrains.java.decompiler.struct.StructRecordComponent;
import org.objectweb.asm.Opcodes;

public class TinyJavadocProvider implements IFabricJavadocProvider {
	final MappingTree tree;
	final int mappingDestNs;
	public TinyJavadocProvider(File tinyFile) {
		try {
			MappingReader.read(tinyFile.toPath(), (MappingVisitor) (tree = new MemoryMappingTree()));
			mappingDestNs = tree.getNamespaceId(ConstantsKt.MAPPING_DEST_NS);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	
	@Override
	public String getClassDoc(StructClass structClass) {
		MappingTree.ClassMapping classDef = getClassMappingFromStructClass(structClass);

		if (classDef == null) {
			return null;
		}

		if (!isRecord(structClass)) {
			return getClassDoc(classDef);
		}

		/**
		 * Handle the record component docs here.
		 *
		 * Record components are mapped via the field name, thus take the docs from the fields and display them on then class.
		 */
		List<String> parts = new ArrayList<>();

		if (classDef.getComment() != null) {
			parts.add(classDef.getComment());
			parts.add("");

			for (String dstNamespace : tree.getDstNamespaces()) {
				parts.add("@mapping %s %s".formatted(dstNamespace, classDef.getDstName(tree.getNamespaceId(dstNamespace))));
			}
		}

		boolean addedParam = false;

		for (StructRecordComponent component : structClass.getRecordComponents()) {
			// The component will always match the field name and descriptor
			MappingTree.FieldMapping fieldDef = classDef.getField(component.getName(), component.getDescriptor(), mappingDestNs);

			if (fieldDef == null) {
				continue;
			}

			String comment = fieldDef.getComment();

			if (comment != null) {
				if (!addedParam && classDef.getComment() != null) {
					//Add a blank line before components when the class has a comment
					parts.add("");
					addedParam = true;
				}

				parts.add(String.format("@param %s %s", fieldDef.getName(mappingDestNs), comment));
			}
		}

		if (parts.isEmpty()) {
			return null;
		}

		return String.join("\n", parts);
	}

	private String getClassDoc(MappingTree.ClassMapping classDef) {
		ArrayList<String> javaDocParts = new ArrayList<>();
		String originalComment = classDef.getComment();
		if (originalComment != null) {
			javaDocParts.add(originalComment);
		}
		MappingTree tree = classDef.getTree();

		javaDocParts.add("");

		for (String dstNamespace : tree.getDstNamespaces()) {
			javaDocParts.add("@mapping %s %s".formatted(dstNamespace, classDef.getDstName(tree.getNamespaceId(dstNamespace))));
		}

		return String.join("\n", javaDocParts);

	}

	@Override
	public String getFieldDoc(StructClass structClass, StructField structField) {
		MappingTree.ClassMapping classDef = getClassMappingFromStructClass(structClass);
		if (classDef == null) {
			return null;
		}
		// None static fields in records are handled in the class javadoc.
		if (isRecord(structClass) && !isStatic(structField)) {
			return null;
		}

		MappingTree.FieldMapping fieldDef = classDef.getField(structField.getName(), structField.getDescriptor(), mappingDestNs);
		return fieldDef != null ? getFieldComment(fieldDef) : null;
	}

	private String getFieldComment(@NotNull MappingTree.FieldMapping fieldDef) {
		ArrayList<String> javaDocParts = new ArrayList<>();
		String originalComment = fieldDef.getComment();
		if (originalComment != null) {
			javaDocParts.add(originalComment);
		}
		MappingTree tree = fieldDef.getTree();

		javaDocParts.add("");

		for (String dstNamespace : tree.getDstNamespaces()) {
			javaDocParts.add("@mapping %s %s".formatted(dstNamespace, fieldDef.getDstName(tree.getNamespaceId(dstNamespace))));
		}

		String mixinForm = "L" + fieldDef.getOwner().getDstName(mappingDestNs) + ";" + fieldDef.getDstName(mappingDestNs) + ":" + fieldDef.getDesc(mappingDestNs);
		javaDocParts.add("@mixin %s".formatted(mixinForm));

		return String.join("\n", javaDocParts);
	}

	private MappingTree.ClassMapping getClassMappingFromStructClass(StructClass structClass) {
		return tree.getClass(structClass.qualifiedName, mappingDestNs);
	}

	@Override
	public String getMethodDoc(StructClass structClass, StructMethod structMethod) {
		MappingTree.ClassMapping classDef = getClassMappingFromStructClass(structClass);
		if (classDef == null) {
			return null;
		}
		MappingTree.MethodMapping methodDef = classDef.getMethod(structMethod.getName(), structMethod.getDescriptor(), mappingDestNs);

		if (methodDef != null) {
			List<String> parts = new ArrayList<>();

			if (methodDef.getComment() != null) {
				parts.add(methodDef.getComment());
			}
			parts.add("");


			String mixinForm = "L" + methodDef.getOwner().getName(mappingDestNs) + ";" + methodDef.getDstName(mappingDestNs) + methodDef.getDesc(mappingDestNs);
			parts.add("@mixin %s".formatted(mixinForm));
			parts.add("");

			for (String dstNamespace : tree.getDstNamespaces()) {
				parts.add("@mapping %s %s".formatted(dstNamespace, methodDef.getDstName(tree.getNamespaceId(dstNamespace))));
			}

			boolean addedParam = false;

			for (var param : methodDef.getArgs()) {
				String comment = param.getComment();

				if (comment != null) {
					if (!addedParam && methodDef.getComment() != null) {
						//Add a blank line before params when the method has a comment
						parts.add("");
						addedParam = true;
					}

					parts.add(String.format("@param %s %s", param.getName(mappingDestNs), comment));
				}
			}

			if (parts.isEmpty()) {
				return null;
			}

			return String.join("\n", parts);
		}

		return null;
	}

	public static boolean isRecord(StructClass structClass) {
		return (structClass.getAccessFlags() & Opcodes.ACC_RECORD) != 0;
	}

	public static boolean isStatic(StructField structField) {
		return (structField.getAccessFlags() & Opcodes.ACC_STATIC) != 0;
	}
}
