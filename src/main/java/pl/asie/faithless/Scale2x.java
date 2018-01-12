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
	private static int get(int[] a, int x, int y, int w, int h, boolean wrap) {
		if (wrap) {
			if (x < 0) x += w;
			else if (x >= w) x -= w;
			if (y < 0) y += h;
			else if (y >= h) y -= h;
		} else {
			if (x < 0 || x >= w || y < 0 || y >= h) {
				return 0;
			}
		}
		return a[y*w+x];
	}

	public static int[] scale(int[] sourcePixels, int width, int height, boolean wrap) {
		int[] targetPixels = new int[sourcePixels.length * 4];

		for (int y = 0; y < height; y++) {
			// Two lines of target pixel pointers
			int tp0 = y * width * 4 - 1;
			int tp1 = tp0 + width * 2;

			// Fill the initial A-I values
			int A = get(sourcePixels, -1, y - 1, width, height, wrap);
			int B = get(sourcePixels, 0, y - 1, width, height, wrap);
			int C = get(sourcePixels, 1, y - 1, width, height, wrap);
			int D = get(sourcePixels, -1, y, width, height, wrap);
			int E = get(sourcePixels, 0, y, width, height, wrap);
			int F = get(sourcePixels, 1, y, width, height, wrap);
			int G = get(sourcePixels, -1, y + 1, width, height, wrap);
			int H = get(sourcePixels, 0, y + 1, width, height, wrap);
			int I = get(sourcePixels, 1, y + 1, width, height, wrap);

			for (int x = 0; x < width; x++)
			{
				if (B != H && D != F)
				{
					targetPixels[++tp0] = D == B ? D : E;
					targetPixels[++tp0] = B == F ? F : E;
					targetPixels[++tp1] = D == H ? D : E;
					targetPixels[++tp1] = H == F ? F : E;
				}
				else
				{
					targetPixels[++tp0] = E;
					targetPixels[++tp0] = E;
					targetPixels[++tp1] = E;
					targetPixels[++tp1] = E;
				}

				// Scroll A-I left
				A = B;
				B = C;
				D = E;
				E = F;
				G = H;
				H = I;

				// Resample rightmost edge
				C = get(sourcePixels, x + 2, y - 1, width, height, wrap);
				F = get(sourcePixels, x + 2, y, width, height, wrap);
				I = get(sourcePixels, x + 2, y + 1, width, height, wrap);
			}
		}

		return targetPixels;
	}
}