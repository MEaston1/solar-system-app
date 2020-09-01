package com.example.engine.util.io;

import android.app.Activity;

import java.io.IOException;
import java.io.InputStream;

public class ProgressMonitorInputStream extends InputStream {

	private final Activity parentActivity;
	private final InputStream stream;

	public ProgressMonitorInputStream(Activity parentActivity, String text, InputStream stream) {
		this.parentActivity = parentActivity;
		this.stream = stream;
	}

	@Override
	public int read() throws IOException {
		return stream.read();
	}
}
