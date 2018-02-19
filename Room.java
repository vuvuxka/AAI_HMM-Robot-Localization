package hmm;

public class Room {
	public final int width, height;

	public Room(int width, int height) {
		this.width = width;
		this.height = height;

		if (width < 1 || height < 1) {
			throw new IllegalArgumentException(
				"Width and height must be atleast one: " +
				"("+ width + ", " + height + ")"
			);
		}

	}

}
