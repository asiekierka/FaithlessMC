package pl.asie.faithless;

@FunctionalInterface
public interface IScalingAlgorithm {
	int[] scale(int[] sourcePixels, int width, int height, boolean wrap);
}
