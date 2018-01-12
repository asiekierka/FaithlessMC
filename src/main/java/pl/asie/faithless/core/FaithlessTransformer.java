/*
 * Copyright (c) 2018 Adrian Siekierka
 *
 * This file is part of Faithless.
 *
 * Faithless is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Faithless is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Faithless.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.asie.faithless.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class FaithlessTransformer implements IClassTransformer {
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if ("net.minecraft.client.renderer.texture.TextureAtlasSprite".equals(transformedName)) {
			ClassReader reader = new ClassReader(basicClass);
			ClassNode node = new ClassNode();
			reader.accept(node, 0);

			for (MethodNode methodNode : node.methods) {
				if ("func_147963_d".equals(methodNode.name) || "generateMipmaps".equals(methodNode.name)) {
					//    ALOAD 1
					//    INVOKESTATIC pl/asie/faithless/ProxyClient.processTexture (Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V
					AbstractInsnNode firstNode = new VarInsnNode(Opcodes.ALOAD, 0);
					AbstractInsnNode secondNode = new MethodInsnNode(
							Opcodes.INVOKESTATIC,
							"pl/asie/faithless/ProxyClient", "processTexture", "(Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V", false
					);
					AbstractInsnNode thirdNode = methodNode.instructions.getFirst();

					methodNode.instructions.insertBefore(thirdNode, secondNode);
					methodNode.instructions.insertBefore(secondNode, firstNode);
					System.out.println("Patched TextureAtlasSprite to process textures without faith! ᕕ(⌐■_■)ᕗ ♪♬");
				}
			}

			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
			node.accept(writer);
			return writer.toByteArray();
		}
		return basicClass;
	}
}
