/**
 * Toolkit - used to be an abstract factory for peers, but since we don't have
 * peers, it is just a simple anchor for central mechanisms (like System-
 * EventQueue etc.) and a wrapper for all native methods. Of course, it is a
 * singleton.
 *
 * Copyright (c) 1998
 *      Transvirtual Technologies, Inc.  All rights reserved.
 *
 * See the file "license.terms" for information on usage and redistribution 
 * of this file. 
 *
 * @author P.C.Mehlitz
 */

package java.awt;

import java.awt.ImageNativeProducer;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.peer.ComponentPeer;
import java.awt.peer.LightweightPeer;
import java.awt.peer.WindowPeer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.Properties;
import kaffe.util.Ptr;
import kaffe.util.log.LogClient;
import kaffe.util.log.LogStream;

class FlushThread
  extends Thread
{
	boolean stop;
	int flushInterval;

FlushThread ( int interval ) {
	super( "AWT-Flusher");
	
	flushInterval = interval;
	setPriority( Thread.MIN_PRIORITY + 1);
}

public void run () {
	while ( !stop ) {
		Toolkit.tlkFlush();
		try {
			Thread.sleep( flushInterval);
		}
		catch ( Exception x ) {}
	}
}

void stopFlushing () {
	stop = true;
}
}

public class Toolkit
{
	static Toolkit singleton;
	static Dimension screenSize;
	static int resolution;
	static EventQueue eventQueue;
	static EventDispatchThread eventThread;
	static NativeClipboard clipboard;
	static LightweightPeer lightweightPeer = new LightweightPeer() {};
	static WindowPeer windowPeer = new WindowPeer() {};
	static boolean isBlocking;
	static boolean isDispatchExclusive;
	static FlushThread flushThread;
	static boolean needsFlush;

static {
	Insets in;
	int ret;

	System.loadLibrary( "awt");

	String tkName = System.getProperty( "display");
	tlkInit( tkName );

	screenSize = new Dimension( tlkGetScreenWidth(), tlkGetScreenHeight());
	resolution = tlkGetResolution();
	
	isBlocking = tlkIsBlocking();
	isDispatchExclusive = tlkIsDispatchExclusive();
	needsFlush = tlkNeedsFlush();

	// we do this here to keep the getDefaultToolkit() method as simple
	// as possible (since it might be called frequently). This is a
	// deviation from the normal Singleton (which initializes the singleton
	// instance upon request)
	singleton = new Toolkit();
	
	eventQueue = new EventQueue();

	// we should refer to Defaults as late as possible (since it may
	// directly or indirectly refer to Toolkit.singleton)
	if ( Defaults.ConsoleClass != null ) {
		redirectStreams();
	}
}

Toolkit () {
}

public void beep () {
	tlkBeep();
}

native static synchronized void cbdFreeClipboard ( Ptr cbdData );

native static synchronized Transferable cbdGetContents( Ptr cbdData);

native static synchronized Ptr cbdInitClipboard ();

native static synchronized boolean cbdSetOwner ( Ptr cbdData );

public int checkImage(Image image, int width, int height, ImageObserver observer) {
	return (image.checkImage( width, height, observer, false));
}

native static synchronized long clrBright ( int rgbValue );

native static synchronized long clrDark ( int rgbValue );

native static synchronized int clrGetPixelValue ( int rgb );

native static synchronized int clrSetSystemColors ( int[] sysClrs );

public Image createImage ( ImageProducer producer ) {
	return new Image( producer);
}

public Image createImage ( byte[] imageData ) {
	return createImage( imageData, 0, imageData.length);
}

public Image createImage ( byte[] imagedata, int imageoffset, int imagelength ) {
	return new Image( imagedata, imageoffset, imagelength);
}

ComponentPeer createLightweight ( Component c ) {
	// WARNING! this is just a dummy to enable checks like
	// "..getPeer() != null.. or ..peer instanceof LightweightPeer..
  // see createWindow()
	return lightweightPeer;
}

protected WindowPeer createWindow ( Window w ) {
	// WARNING! this is just a dummy to enable checks like
	// "..getPeer() != null.. or ..peer instanceof LightweightPeer..
	// it is NOT a real peer support. The peer field just exists to
	// check if a Component already passed its addNotify()/removeNotify().
	// This most probably will be removed once the "isLightweightComponent()"
	// method gets official (1.2?)

	return windowPeer;
}

native static synchronized AWTEvent evtGetNextEvent ();

native static synchronized Component[] evtInit ();

native static synchronized AWTEvent evtPeekEvent ();

native static synchronized AWTEvent evtPeekEventId ( int eventId );

native static synchronized int evtRegisterSource ( Ptr wndData );

native static synchronized int evtUnregisterSource ( Ptr wndData );

native static synchronized void evtWakeup ();

native static synchronized int fntBytesWidth ( Ptr fmData, byte[] data, int off, int len );

native static synchronized int fntCharWidth ( Ptr fmData, char c );

native static synchronized int fntCharsWidth ( Ptr fmData, char[] data, int off, int len );

native static synchronized void fntFreeFont ( Ptr fontData );

native static synchronized void fntFreeFontMetrics ( Ptr fmData );

native static synchronized int fntGetAscent ( Ptr fmData);

native static synchronized int fntGetDescent ( Ptr fmData);

native static synchronized int fntGetFixedWidth ( Ptr fmData);

native static synchronized int fntGetHeight ( Ptr fmData);

native static synchronized int fntGetLeading ( Ptr fmData);

native static synchronized int fntGetMaxAdvance ( Ptr fmData);

native static synchronized int fntGetMaxAscent ( Ptr fmData);

native static synchronized int fntGetMaxDescent ( Ptr fmData);

native static synchronized int[] fntGetWidths ( Ptr fmData );

native static synchronized Ptr fntInitFont ( String fntSpec, int style, int size );

native static synchronized Ptr fntInitFontMetrics ( Ptr fontData);

native static synchronized boolean fntIsWideFont ( Ptr fmData);

native static synchronized int fntStringWidth ( Ptr fmData, String s );

public ColorModel getColorModel () {
	return null;
}

public static Toolkit getDefaultToolkit () {
	return singleton;
}

public String[] getFontList () {
	String[] list = { "Default", "Monospaced",
	                  "SansSerif", "Serif",
			              "Dialog", "DialogInput", "ZapfDingbats" };
	return list;
}

public FontMetrics getFontMetrics ( Font font ) {
	return FontMetrics.getFontMetrics( font);
}

public Image getImage ( String filename ) {
	return new Image( new File( filename));
}

public Image getImage ( URL url ) {
	return new Image( url);
}

public int getMenuShortcutKeyMask() {
	return InputEvent.CTRL_MASK;
}

public PrintJob getPrintJob ( Frame frame, String jobtitle, Properties props ) {
	return new PSPrintJob( frame, jobtitle, props);
}

public static String getProperty ( String key, String defaultValue ) {
	return null;
}

public Dimension getScreenSize () {
	return screenSize;
}

public Clipboard getSystemClipboard () {
	if ( clipboard == null )
		clipboard = new NativeClipboard();
		
	return clipboard;
}

public EventQueue getSystemEventQueue () {
	return eventQueue;
}

native static synchronized void graAddClip ( Ptr grData, int xClip, int yClip, int wClip, int hClip );

native static synchronized void graClearRect ( Ptr grData, int x, int y, int width, int height );

native static synchronized void graCopyArea ( Ptr grData, int x, int y, int width, int height,
					   int xDelta, int yDelta );

native static synchronized void graDraw3DRect ( Ptr grData, int x, int y, int width, int height,
		     boolean raised, int rgbValue );

native static synchronized void graDrawArc ( Ptr grData, int x, int y, int width, int height, int startAngle, int arcAngle );

native static synchronized void graDrawBytes ( Ptr grData, byte[] data, int offset, int length, int x, int y );

native static synchronized void graDrawChars ( Ptr grData, char[] data, int offset, int length, int x, int y );

native static synchronized void graDrawImage( Ptr grData, Ptr imgData, int src_x, int src_y, int dst_x, int dst_y, int w, int h, int bgval );

native static synchronized void graDrawImageScaled ( Ptr grData, Ptr imgData,
			  int dx0, int dy0, int dx1, int dy1,
			  int sx0, int sy0, int sx1, int sy1, int bgval );

native static synchronized void graDrawLine ( Ptr grData, int x1, int y1, int x2, int y2 );

native static synchronized void graDrawOval ( Ptr grData, int x, int y, int width, int height );

native static synchronized void graDrawPolygon ( Ptr grData, int[] xPoints, int[] yPoints, int nPoints );

native static synchronized void graDrawPolyline ( Ptr grData, int[] xPoints, int[] yPoints, int nPoints );

native static synchronized void graDrawRect ( Ptr grData, int x, int y, int width, int height );

native static synchronized void graDrawRoundRect ( Ptr grData, int x, int y, int width, int height,
						int arcWidth, int arcHeight );

native static synchronized void graDrawString ( Ptr grData, String s, int x, int y );

native static synchronized void graFill3DRect ( Ptr grData, int x, int y, int width, int height,
		     boolean raised, int rgbValue );

native static synchronized void graFillArc ( Ptr grData, int x, int y, int width, int height, int startAngle, int arcAngle );

native static synchronized void graFillOval ( Ptr grData, int x, int y, int width, int height );

native static synchronized void graFillPolygon ( Ptr grData, int[] xPoints, int[] yPoints, int nPoints );

native static synchronized void graFillRect ( Ptr grData, int x, int y, int width, int height );

native static synchronized void graFillRoundRect ( Ptr grData, int x, int y, int width, int height,
						int arcWidth, int arcHeight );

native static synchronized void graFreeGraphics ( Ptr grData );

native static synchronized Ptr graInitGraphics ( Ptr grData, Ptr tgtData, int tgtType,
					      int xOffset, int yOffset,
					      int xClip, int yClip, int wClip, int hClip,
					      Ptr fontData, int fgClr, int bgClr, boolean blank );

native static synchronized void graSetBackColor ( Ptr grData, int pixelValue );

native static synchronized void graSetClip ( Ptr grData, int xClip, int yClip, int wClip, int hClip );

native static synchronized void graSetColor ( Ptr grData, int pixelValue );

native static synchronized void graSetFont ( Ptr grData, Ptr fontData );

native static synchronized void graSetOffset ( Ptr grData, int xOffset, int yOffset );

native static synchronized void graSetPaintMode ( Ptr grData );

native static synchronized void graSetVisible ( Ptr grData, boolean isVisible );

native static synchronized void graSetXORMode ( Ptr grData, int xClr );

native static synchronized void imgComplete ( Ptr imgData, int status );

native static synchronized Ptr imgCreateFromData( byte[] buf, int offset, int len);

native static synchronized Ptr imgCreateFromFile( String gifPath);

native static synchronized Ptr imgCreateImage( int w, int h);

native static synchronized Ptr imgCreateScaledImage( Ptr imgData, int w, int h);

native static synchronized Ptr imgCreateScreenImage( int w, int h);

native static synchronized void imgFreeImage ( Ptr imgData );

native static synchronized int imgGetHeight( Ptr imgData);

native static synchronized int imgGetLatency ( Ptr imgData );

native static synchronized Ptr imgGetNextFrame ( Ptr imgData );

native static synchronized int imgGetWidth( Ptr imgData);

native static synchronized boolean imgIsMultiFrame( Ptr imgData);

native static synchronized void imgProduceImage( ImageNativeProducer prod, Ptr imgData);

native static synchronized void imgSetIdxPels( Ptr imgData, int x, int y, int w, int h, int[] rgbs, byte[] pels, int trans, int off, int scans);

native static synchronized void imgSetRGBPels( Ptr imgData, int x, int y, int w, int h, int[] rgbs, int off, int scans);

protected void loadSystemColors ( int[] sysColors ) {
	clrSetSystemColors( sysColors);
}

public boolean prepareImage ( Image image, int width, int height, ImageObserver observer ) {
	return (image.loadImage( width, height, observer));
}

static void redirectStreams () {
	try {
		LogClient lv = (LogClient) Class.forName( Defaults.ConsoleClass).newInstance();
		LogStream ls = new LogStream( 1, lv);
		lv.enable();
	
		System.setOut( new PrintStream( ls) );
		System.setErr( System.out);
	}
	catch ( Exception x ) {
		System.err.println( "unable to redirect out, err");
		x.printStackTrace();
	}
}

static void startDispatch () {
	if ( eventThread == null ) {
		eventThread = new EventDispatchThread( eventQueue);
		eventThread.start();
	}

	if ( needsFlush && (flushThread == null) ){
		flushThread = new FlushThread( Defaults.GraFlushRate);
		flushThread.start();
	}
}

static void stopDispatch () {
	if ( eventThread != null ) {
		eventThread.stopDispatching();
		eventThread = null;
	}
	
	if ( flushThread != null ){
		flushThread.stopFlushing();
		flushThread = null;
	}
}

public void sync () {
	tlkSync();
}

static void terminate () {
	if ( clipboard != null )
		clipboard.dispose();

	stopDispatch();
	tlkTerminate();
}

native static synchronized void tlkBeep ();

native static synchronized void tlkDisplayBanner ( String banner );

native static synchronized void tlkFlush ();

native static synchronized int tlkGetResolution ();

native static synchronized int tlkGetScreenHeight ();

native static synchronized int tlkGetScreenWidth ();

native static synchronized void tlkInit ( String displayName );

native static synchronized boolean tlkIsBlocking();

native static synchronized boolean tlkIsDispatchExclusive();

native static synchronized boolean tlkNeedsFlush();

native static synchronized void tlkSync ();

native static synchronized void tlkTerminate ();

native static synchronized String tlkVersion ();

native static synchronized Ptr wndCreateDialog ( Ptr ownerData, String title, int x, int y, int width, int height,
		      int cursorType, int bgColor, boolean isResizable );

native static synchronized Ptr wndCreateFrame ( String title, int x, int y, int width, int height,
		     int cursorType, int bgColor, boolean isResizable );

native static synchronized Ptr wndCreateWindow ( Ptr ownerData, int x, int y, int width, int height,
		      int cursorType, int bgColor );

native static synchronized void wndDestroyWindow ( Ptr wndData );

native static synchronized void wndRepaint ( Ptr wndData, int x, int y, int width, int height );

native static synchronized void wndRequestFocus ( Ptr wndData );

native static synchronized void wndSetBounds ( Ptr wndData, int x, int y, int width, int height, boolean isResizable );

native static synchronized void wndSetCursor ( Ptr wndData, int cursorType );

native static synchronized int wndSetDialogInsets ( int top, int left, int bottom, int right);

native static synchronized int wndSetFrameInsets ( int top, int left, int bottom, int right);

native static synchronized void wndSetIcon ( Ptr wndData, Ptr iconData );

native static synchronized void wndSetResizable ( Ptr wndData, boolean isResizable,
		       int x, int y, int width, int height );

native static synchronized void wndSetTitle ( Ptr wndData, String title );

native static synchronized void wndSetVisible ( Ptr wndData, boolean showIt );

native static synchronized void wndToBack ( Ptr wndData );

native static synchronized void wndToFront ( Ptr wndData );
}
