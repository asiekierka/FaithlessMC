/**
 * Scale2x.java
 *
 * Written by Markus Persson of Mojang Specifications for a super mario programming contest.
 * Implements the Scale2x algorithm described here: http://scale2x.sourceforge.net/algorithm.html
 * Works on any input image size, and (no longer! ~ asie) uses a fancy border hack to prevent range checking.
 * It's (no longer! ~ asie) fast enough for real time use on smaller images (320x240 and thereabouts)
 *
 * This code is public domain. Do whatever you want with it.
 *
 * Improved further by asie to utilize in the Faithless mod.
 */
package pl.asie.faithless;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public final class Scale2x {
	public static int[] scale(int[] sourcePixels, int width, int height, boolean wrap) {
		int[] targetPixels = new int[sourcePixels.length * 4];

		for (int y = 0; y < height; y++) {
			// Two lines of target pixel pointers
			int tp0 = y * width * 4 - 1;
			int tp1 = tp0 + width * 2;

			// Fill the initial A-I values
			// int A = Faithless.get(sourcePixels, -1, y - 1, width, height, wrap);
			int B = Faithless.get(sourcePixels, 0, y - 1, width, height, wrap);
			int C = Faithless.get(sourcePixels, 1, y - 1, width, height, wrap);
			int D = Faithless.get(sourcePixels, -1, y, width, height, wrap);
			int E = Faithless.get(sourcePixels, 0, y, width, height, wrap);
			int F = Faithless.get(sourcePixels, 1, y, width, height, wrap);
			// int G = Faithless.get(sourcePixels, -1, y + 1, width, height, wrap);
			int H = Faithless.get(sourcePixels, 0, y + 1, width, height, wrap);
			int I = Faithless.get(sourcePixels, 1, y + 1, width, height, wrap);

			for (int x = 0; x < width; x++)
			{
				if (!Faithless.compareEqual(B, H) && !Faithless.compareEqual(D, F))
				{
					targetPixels[++tp0] = Faithless.compareEqual(D, B) ? D : E;
					targetPixels[++tp0] = Faithless.compareEqual(B, F) ? F : E;
					targetPixels[++tp1] = Faithless.compareEqual(D, H) ? D : E;
					targetPixels[++tp1] = Faithless.compareEqual(H, F) ? F : E;
				}
				else
				{
					targetPixels[++tp0] = E;
					targetPixels[++tp0] = E;
					targetPixels[++tp1] = E;
					targetPixels[++tp1] = E;
				}

				// Scroll A-I left
				// A = B;
				B = C;
				D = E;
				E = F;
				// G = H;
				H = I;

				// Resample rightmost edge
				C = Faithless.get(sourcePixels, x + 2, y - 1, width, height, wrap);
				F = Faithless.get(sourcePixels, x + 2, y, width, height, wrap);
				I = Faithless.get(sourcePixels, x + 2, y + 1, width, height, wrap);
			}
		}

		return targetPixels;
	}
}