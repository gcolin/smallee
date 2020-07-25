package net.gcolin.rest.multipart;

import java.util.ArrayList;
import java.util.List;

public class MultiPart {

	private final List<BodyPart> parts = new ArrayList<>();
	
	public List<BodyPart> getParts() {
		return parts;
	}
	
}
