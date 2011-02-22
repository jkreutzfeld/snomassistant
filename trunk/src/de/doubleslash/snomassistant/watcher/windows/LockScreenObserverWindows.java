package de.doubleslash.snomassistant.watcher.windows;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import de.doubleslash.snomassistant.Controller;
import de.doubleslash.snomassistant.watcher.LockScreenObserver;

public class LockScreenObserverWindows implements LockScreenObserver, Runnable {

	private Controller controller;

	private Thread thread;

	private URI exe;

	public LockScreenObserverWindows(Controller c) {
		this.controller = c;

		URI uri = null;

		try {
			uri = getJarURI();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			this.exe = getFile(uri, "EventListener.exe");
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.thread = new Thread(this);
		thread.start();

	}

	public void kill() {
		this.thread.interrupt();
	}

	@Override
	public void run() {
		URI dir = this.exe;
		System.out.println(dir);
		Runtime run = Runtime.getRuntime();
		Process exec = null;
		try {
			exec = run.exec(dir.getPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new Thread(new EventReader(controller, exec.getInputStream())).start();
		try {
			exec.waitFor();
		} catch (InterruptedException e) {
			System.out.println("EventReader interrupted.");
			exec.destroy();
		}
	}

	private URI getJarURI() throws URISyntaxException {
		final ProtectionDomain domain;
		final CodeSource source;
		final URL url;
		final URI uri;

		domain = LockScreenObserverWindows.class.getProtectionDomain();
		source = domain.getCodeSource();
		url = source.getLocation();
		uri = url.toURI();

		return (uri);
	}

	private URI getFile(final URI where, final String fileName)
			throws ZipException, IOException {
		final File location;
		final URI fileURI;

		location = new File(where);

		// not in a JAR, just return the path on disk
		if (location.isDirectory()) {
			fileURI = URI.create(where.toString() + fileName);
		} else {
			final ZipFile zipFile;

			zipFile = new ZipFile(location);

			try {
				fileURI = extract(zipFile, fileName);
			} finally {
				zipFile.close();
			}
		}

		return (fileURI);
	}

	private URI extract(final ZipFile zipFile, final String fileName)
			throws IOException {
		final File tempFile;
		final ZipEntry entry;
		final InputStream zipStream;
		OutputStream fileStream;

		tempFile = File.createTempFile(fileName,
				Long.toString(System.currentTimeMillis()));
		tempFile.deleteOnExit();
		entry = zipFile.getEntry(fileName);

		if (entry == null) {
			throw new FileNotFoundException("cannot find file: " + fileName
					+ " in archive: " + zipFile.getName());
		}

		zipStream = zipFile.getInputStream(entry);
		fileStream = null;

		try {
			final byte[] buf;
			int i;

			fileStream = new FileOutputStream(tempFile);
			buf = new byte[1024];
			i = 0;

			while ((i = zipStream.read(buf)) != -1) {
				fileStream.write(buf, 0, i);
			}
		} finally {
			close(zipStream);
			close(fileStream);
		}

		return (tempFile.toURI());
	}

	private void close(final Closeable stream) {
		if (stream != null) {
			try {
				stream.close();
			} catch (final IOException ex) {
				ex.printStackTrace();
			}
		}
	}

}

class EventReader implements Runnable {

	private Controller controller;
	private InputStream input;

	EventReader(Controller c, InputStream in) {
		this.controller = c;
		this.input = in;
	}

	@Override
	public void run() {
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(
				input));

		String s;
		// read the output from the command
		try {
			while ((s = stdInput.readLine()) != null) {
				switch (Integer.parseInt(s)) {

				case 4800:
				case 4802:
					controller.setIdentityStatus(false);
					break;
				case 4801:
				case 4803:
					controller.setIdentityStatus(true);
					break;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}