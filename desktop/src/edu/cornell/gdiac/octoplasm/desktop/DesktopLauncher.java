package edu.cornell.gdiac.octoplasm.desktop;

import edu.cornell.gdiac.backend.*;
import edu.cornell.gdiac.octoplasm.GDXRoot;

public class DesktopLauncher {
	public static void main (String[] arg) {
		// This is the new application wrapper
		GDXAppSettings config = new GDXAppSettings();
		config.useHDPI = false;
		config.title = "Octoplasm";
		config.resizable  = true;
		config.forceExit = true;
		config.vSyncEnabled = false;
		new GDXApp( new GDXRoot(), config );
	}
}
