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

package pl.asie.faithless;

// https://forums.libretro.com/t/scalenx-artifact-removal-and-algorithm-improvement/1686/6
public final class Scale2xSFX {
	public static int[] scale(int[] sourcePixels, int width, int height, boolean wrap) {
		int[] targetPixels = new int[sourcePixels.length * 4];

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int A = Faithless.get(sourcePixels, x - 1, y - 1, width, height, wrap);
				int B = Faithless.get(sourcePixels, x, y - 1, width, height, wrap);
				int C = Faithless.get(sourcePixels, x + 1, y - 1, width, height, wrap);
				int D = Faithless.get(sourcePixels, x - 1, y, width, height, wrap);
				int E = Faithless.get(sourcePixels, x, y, width, height, wrap);
				int F = Faithless.get(sourcePixels, x + 1, y, width, height, wrap);
				int G = Faithless.get(sourcePixels, x - 1, y + 1, width, height, wrap);
				int H = Faithless.get(sourcePixels, x, y + 1, width, height, wrap);
				int I = Faithless.get(sourcePixels, x + 1, y + 1, width, height, wrap);
				int J = Faithless.get(sourcePixels, x, y - 2, width, height, wrap);
				int K = Faithless.get(sourcePixels, x - 2, y, width, height, wrap);
				int L = Faithless.get(sourcePixels, x + 2, y, width, height, wrap);
				int M = Faithless.get(sourcePixels, x, y + 2, width, height, wrap);

				boolean equalBD = Faithless.compareEqual(B, D);
				boolean equalBF = Faithless.compareEqual(B, F);
				boolean equalDH = Faithless.compareEqual(D, H);
				boolean equalFH = Faithless.compareEqual(F, H);
				
				Faithless.set(targetPixels, x * 2 + 0, y * 2 + 0, width*2, height*2,
						equalBD && !equalBF && !equalDH
						&& (!Faithless.compareEqual(E, A) || Faithless.compareEqual(E, C) || Faithless.compareEqual(E, G) || Faithless.compareEqual(A, J) || Faithless.compareEqual(A, K))
						? D : E);
				Faithless.set(targetPixels, x * 2 + 1, y * 2 + 0, width*2, height*2,
						equalBF && !equalBD && !equalFH
								&& (!Faithless.compareEqual(E, C) || Faithless.compareEqual(E, A) || Faithless.compareEqual(E, I) || Faithless.compareEqual(C, J) || Faithless.compareEqual(C, K))
								? F : E);
				Faithless.set(targetPixels, x * 2 + 0, y * 2 + 1, width*2, height*2,
						equalDH && !equalBD && !equalFH
								&& (!Faithless.compareEqual(E, G) || Faithless.compareEqual(E, A) || Faithless.compareEqual(E, I) || Faithless.compareEqual(G, K) || Faithless.compareEqual(G, M))
								? D : E);
				Faithless.set(targetPixels, x * 2 + 1, y * 2 + 1, width*2, height*2,
						equalFH && !equalBF && !equalDH
								&& (!Faithless.compareEqual(E, I) || Faithless.compareEqual(E, C) || Faithless.compareEqual(E, G) || Faithless.compareEqual(I, L) || Faithless.compareEqual(I, M))
								? F : E);
			}
		}

		return targetPixels;
	}
}