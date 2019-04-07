import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.image.MemoryImageSource;
import javax.swing.*;
import java.lang.Math;
import java.applet.Applet;

public class AnimateMandelbrot extends Applet 
                               implements Runnable {
    public static final long serialVersionUID = 24362462L;
    int frame;
    int delay;
    Thread animator;

    Image offImage;
    Graphics offGraphics;
    
    Mandelbrot M;
    Image image;

    MouseMan MsMan;
    
    MemoryImageSource source;

    //    private int XCanvasSize = 540;
    //    private int YCanvasSize = 432;

    private int XCanvasSize = 640;
    private int YCanvasSize = 500;

    private float Step = (float) 3.0/XCanvasSize;

    private float XMin      = (float) -2.0;
    private float YMin      = (float) -1.2;

    private float ZoomRate = (float) 0.05;
    private float HalfZoomRate = ZoomRate/(float) 2.0;
    private float ZoomInFactor = (float) 1.0 - ZoomRate;
    private float ZoomOutFactor = (float) 1.0 + ZoomRate;

    private float PressedAtXMin;
    private float PressedAtYMin;

    private long zoomedframes = 0;
    private long deltasum = 0;

    public AnimateMandelbrot() {
        MsMan = new MouseMan();
        addMouseMotionListener(MsMan);
        addMouseListener(MsMan);
    }

    public static void main(String s[]) {
        Frame f = new Frame("AnimateMandelbrot");
        f.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {System.exit(0);}
            });

        AnimateMandelbrot applet = new AnimateMandelbrot();

        // Do init()'s job
        applet.delay = 33;

	applet.start();

	f.add("Center", applet);
	f.pack();
        f.setSize(new Dimension(applet.XCanvasSize, applet.YCanvasSize+28));
        //f.show();

        applet.run();
    }

    /**
     * Initialize the applet and compute the delay between frames.
     */
    public void init() {
	String str = getParameter("fps");
	int fps = (str != null) ? Integer.parseInt(str) : 30;
	delay = (fps > 0) ? (1000 / fps) : 33;
    }

    /**
     * This method is called when the applet becomes visible on
     * the screen. Create a thread and start it.
     */
    public void start() {
	animator = new Thread(this);
	animator.start();

        M = new Mandelbrot();
        M.SetXMinYMinStep(XMin, YMin, Step);
        M.SetCanvasSize(XCanvasSize, YCanvasSize);
        M.Compute();
        source = new MemoryImageSource(XCanvasSize, YCanvasSize, 
                                       M.GetPixels(), 0, 
                                       XCanvasSize);
        source.setAnimated(true);
        //source.setFullBufferUpdates(true);
        image = Toolkit.getDefaultToolkit().createImage(source);
    }

    /**
     * This method is called by the thread that was created in
     * the start method. It does the main animation.
     */
    public void run() {
	// Remember the starting time
	long tm = System.currentTimeMillis();
	while (Thread.currentThread() == animator) {
	    // Display the next frame of animation.
	    repaint();
	    // Delay depending on how far we are behind.
	    try {
		tm += delay;
		Thread.sleep(Math.max(0, tm - System.currentTimeMillis()));
	    } catch (InterruptedException e) {
		break;
	    }
	    // Advance the frame
	    frame++;
	}
    }

    /**
     * This method is called when the applet is no longer
     * visible. Set the animator variable to null so that the
     * thread will exit before displaying the next frame.
     */
    public void stop() {
	animator = null;
	offImage = null;
	offGraphics = null;
    }

    /**
     * Paint the previous frame (if any).
     */
    public void paint(Graphics g) {
	update(g);
    }

    /**
     * Update a frame of animation.
     */
    public void update(Graphics g) {
        long stm = System.currentTimeMillis();

	// Create the offscreen graphics context
	if (offGraphics == null) {
	    offImage = createImage(XCanvasSize, YCanvasSize);
	    offGraphics = offImage.getGraphics();
	}

	// Erase the previous image
	offGraphics.setColor(getBackground());
	offGraphics.fillRect(0, 0, XCanvasSize, YCanvasSize);
	//offGraphics.setColor(Color.black);

	// Paint the frame into the image
	paintFrame(offGraphics);

	// Paint the image onto the screen
	g.drawImage(offImage, 0, 0, null);

    }

    /**
     * Paint a frame of animation.
     */
    public void paintFrame(Graphics g) {
        if ((( MsMan.LeftButtonOn == true) && ( MsMan.RightButtonOn == true)) || 
            (MsMan.MiddleButtonOn == true)) {

            if (MsMan.Pressed == true) {
                MsMan.Pressed = false;
                PressedAtXMin = XMin;
                PressedAtYMin = YMin;
            }
            if (MsMan.Dragged == true) {
                MsMan.Dragged = false;
                int deltaX = MsMan.lastDraggedX - MsMan.lastPressedX;
                int deltaY = MsMan.lastDraggedY - MsMan.lastPressedY;
                XMin = PressedAtXMin - (Step * deltaX);
                YMin = PressedAtYMin - (Step * deltaY);
                M.SetXMinYMin(XMin,YMin);
            }
        }
        else if (( MsMan.LeftButtonOn == true) || ( MsMan.RightButtonOn == true)) {

            //float lastPressedX = XMin + Step * MsMan.lastPressedX;
            //float lastPressedY = YMin + Step * (YCanvasSize - MsMan.lastPressedY);
            //System.out.println("lastPressedX: " + MsMan.lastPressedX + 
            //                   " " + lastPressedX + 
            //                   " " + "lastPressedY: " + MsMan.lastPressedY + 
            //                   " " + lastPressedY); 

            float ZoomFactor = (MsMan.LeftButtonOn == true)?ZoomInFactor:ZoomOutFactor; 
            XMin = XMin + (Step * MsMan.lastMouseX * (1 - ZoomFactor));
            YMin = YMin + (Step * MsMan.lastMouseY * (1 - ZoomFactor));
            Step = Step * ZoomFactor;
            M.SetXMinYMinStep(XMin, YMin, Step);
        }
        
        if (( MsMan.LeftButtonOn == true) || ( MsMan.RightButtonOn == true) || 
            ( MsMan.MiddleButtonOn == true)) {
            long tm = System.currentTimeMillis();
            M.Compute();
            long delta = System.currentTimeMillis() - tm;
            deltasum += delta;
            zoomedframes++;
            //:System.out.println("Delta is: " + delta); 
            //:System.out.println("FPS is: " + (float) 1000*zoomedframes/deltasum); 
        }
        else if (MsMan.Released == true) {
            MsMan.Released = false;
            M.ComputeWithoutSolidGuessing();
        }

        source.newPixels();

        g.drawImage(image, 0, 0, XCanvasSize, 
                    YCanvasSize, null);

        //g.drawString("Frame " + frame, 0, 30);

    }
}


class MouseMan implements MouseListener, 
                          MouseMotionListener {

    boolean LeftButtonOn = false;
    boolean MiddleButtonOn = false;
    boolean RightButtonOn = false;
    boolean Pressed = false;
    boolean Dragged = false;
    boolean Released = false;

    int lastPressedX, lastPressedY;
    int lastDraggedX, lastDraggedY;
    int lastMouseX, lastMouseY;

    // Handles the event of the user pressing down the mouse button.
    public void mousePressed(MouseEvent e){
        Pressed = true;
        Dragged = false;

        if (e.getButton() == MouseEvent.BUTTON2) {
            MiddleButtonOn = true;
        }

        if (e.getButton() == MouseEvent.BUTTON1) {
            LeftButtonOn = true;
        }

        if (e.getButton() == MouseEvent.BUTTON3) {
            RightButtonOn = true;
        }

        lastMouseX = lastPressedX = e.getX();
        lastMouseY = lastPressedY = e.getY();
    }

    // Handles the event of a user dragging the mouse while holding 
    // down the mouse button.
    public void mouseDragged(MouseEvent e){

        Dragged = true;
        lastMouseX = lastDraggedX = e.getX();
        lastMouseY = lastDraggedY = e.getY();

    }

    // Handles the event of a user releasing the mouse button.
    public void mouseReleased(MouseEvent e){
        Pressed = false;
        Dragged = false;
        Released = true;

        if (e.getButton() == MouseEvent.BUTTON1) {
            LeftButtonOn = false;
        }

        if (e.getButton() == MouseEvent.BUTTON3) {
            RightButtonOn = false;
        }
    }
    
    // This method required by MouseListener.
    public void mouseMoved(MouseEvent e){}
    
    // These methods are required by MouseMotionListener.
    public void mouseClicked(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    public void mouseEntered(MouseEvent e){}
}


class Mandelbrot {

    int ComputeMethod = 5;
    private int XCanvasSize = 640;
    private int YCanvasSize = 500;

    private float Step = (float) 3.0/XCanvasSize;

    private float XMin      = (float) -2.0;
    private float YMin      = (float) -1.2;

    private float XMax      =  (Step * XCanvasSize) + XMin;
    private float YMax      =  (Step * YCanvasSize) + YMin;;

    private int[] pixels = new int[XCanvasSize * YCanvasSize];

    private int MaxIters = 170;

    private float PrevXMin      = (float) -2.0;
    private float PrevYMin      = (float) -1.2;
    private float PrevStep      = (float) 3.0/XCanvasSize;

    private float[][] PrevXVal;
    private float[][] PrevYVal;
    private int[][] newpixels = new int[XCanvasSize][YCanvasSize];

    public void SetCanvasSize (int XCanvasSize_i, int YCanvasSize_i) {
        XCanvasSize = XCanvasSize_i;
        YCanvasSize = YCanvasSize_i;
	Step = (float) 3.0/XCanvasSize;
	PrevStep      = (float) 3.0/XCanvasSize;
        XMax        =  (Step * XCanvasSize) + XMin;
        YMax        =  (Step * YCanvasSize) + YMin;;
        pixels = new int[XCanvasSize * YCanvasSize];
    }

    public void SetStep(float Step_i) {
        Step = Step_i;
        XMax        =  (Step * XCanvasSize) + XMin;
        YMax        =  (Step * YCanvasSize) + YMin;;
    }

    public void SetXMinYMin(float XMin_i, float YMin_i) {
        XMin        =  XMin_i;
        YMin        =  YMin_i;
        XMax        =  (Step * XCanvasSize) + XMin;
        YMax        =  (Step * YCanvasSize) + YMin;;
    }

    public void SetXMinYMinStep(float XMin_i, 
                                float YMin_i,
                                float Step_i) {
        XMin        =  XMin_i;
        YMin        =  YMin_i;
        Step = Step_i;
        XMax        =  (Step * XCanvasSize) + XMin;
        YMax        =  (Step * YCanvasSize) + YMin;;
    }

    public void SetMaxIter(int MaxIters_i) {
        MaxIters = MaxIters_i;
    }

    public int[] GetPixels() {
        return pixels;
    }
    
    public String toString() {
        return "Mandelbrot "  + "XMin: " + XMin 
            + "XMax: " + XMax 
            + "YMin: " + YMin 
            + "YMax: " + YMax 
            + "MaxIters: " + MaxIters 
            + "XCanvasSize: " + XCanvasSize 
            + "YCanvasSize: " + YCanvasSize;
    }
    public void Compute() {
        switch (ComputeMethod) {
        case 1:
            ComputeWithSolidGuessing1();
            break;
        case 2:
            ComputeWithSolidGuessing2();
            break;
        case 3:
            ComputeWithSolidGuessing3();
            break;
        case 4:
            ComputeByPixelMoving();
            break;
        case 5:
            ComputeByPixelMovingAndSolidGuessing3();
            break;
        case 0:            
        default:
            ComputeWithoutSolidGuessing();
        }
    }
    public void ComputeWithoutSolidGuessing() {

        float[] CImaginary = new float[YCanvasSize];
        float[] CReal = new float[XCanvasSize];

        // Precompute Real component and imaginary component
	for (int y = 0; y < YCanvasSize; y++) {
            CImaginary[y] = YMin + y * Step;
        }

        for (int x = 0; x < XCanvasSize; x++) {
            CReal[x] = XMin + x * Step;
        }
        
	for (int y = 0; y < YCanvasSize; y++) {
	    for (int x = 0; x < XCanvasSize; x++) {
                PixelProperty p = MandelbrotinnerLoop(CReal[x], CImaginary[y]);
                pixels[y*XCanvasSize + x] = 
                    (p.inset == true)?InColor.GetPixelValue(p):
                    OutColor.GetPixelValue(p);
            }
        }
    }

    public void ComputeWithSolidGuessing1() {

        float[] CImaginary = new float[YCanvasSize];
        float[] CReal = new float[XCanvasSize];

        // Precompute Real component and imaginary component
	for (int y = 0; y < YCanvasSize; y++) {
            CImaginary[y] = YMin + y * Step;
        }

        for (int x = 0; x < XCanvasSize; x++) {
            CReal[x] = XMin + x * Step;
        }
        
	for (int y = 0; y < YCanvasSize/2; y++) {
	    for (int x = 0; x < XCanvasSize; x++) {
                PixelProperty p = MandelbrotinnerLoop(CReal[x], CImaginary[2*y]);
                pixels[2*y*XCanvasSize + x] = 
                    (p.inset == true)?InColor.GetPixelValue(p):
                    OutColor.GetPixelValue(p);
                if ((x == 0) || (y == (YCanvasSize/2 - 1))) {
                    p = MandelbrotinnerLoop(CReal[x], CImaginary[2*y+1]);
                    pixels[(2*y+1)*XCanvasSize + x] = 
                        (p.inset == true)?InColor.GetPixelValue(p):
                        OutColor.GetPixelValue(p);
                }
            }
        }
        
	for (int y = 0; y < YCanvasSize/2; y++) {
	    for (int x = 0; x < XCanvasSize; x++) {
                if ((x == 0) || (y == (YCanvasSize/2 - 1))) {

                }
                else {
                    int N = pixels[(2*y)*XCanvasSize + x];
                    int S = pixels[(2*y+2)*XCanvasSize + x];
                    int E = pixels[(2*y+1)*XCanvasSize + x-1];
                    int W = pixels[(2*y+1)*XCanvasSize + x+1];
                    int NE = pixels[(2*y)* XCanvasSize + x-1];
                    int NW = pixels[(2*y)* XCanvasSize + x+1];
                    int SE = pixels[(2*y+2)* XCanvasSize + x-1];
                    int SW = pixels[(2*y+2)* XCanvasSize + x+1];
                    if ((N == S) && (E == W) && (N == E)) {
                        if ((NE == NW) && (SE == SW) && (NE == SE) && (N == NE)) {
                            pixels[(2*y+1)* XCanvasSize + x] =  pixels[(2*y+1)*XCanvasSize + x-1];
                        }
                    }
                    else {
                        PixelProperty p = MandelbrotinnerLoop(CReal[x], CImaginary[2*y+1]);
                        pixels[(2*y+1)*XCanvasSize + x] = 
                            (p.inset == true)?InColor.GetPixelValue(p):
                            OutColor.GetPixelValue(p);
                    }
                }
            }
        }
    }
    
    public void ComputeWithSolidGuessing2() {

        float[] CImaginary = new float[YCanvasSize];
        float[] CReal = new float[XCanvasSize];

        // Precompute Real component and imaginary component
	for (int y = 0; y < YCanvasSize; y++) {
            CImaginary[y] = YMin + y * Step;
        }

        for (int x = 0; x < XCanvasSize; x++) {
            CReal[x] = XMin + x * Step;
        }

        // Fill in a grid pattern with the borders filled in.
	for (int y = 0; y < YCanvasSize/2; y++) {
	    for (int x = 0; x < XCanvasSize/2; x++) {
                PixelProperty p = MandelbrotinnerLoop(CReal[2*x], CImaginary[2*y]);
                pixels[2*y*XCanvasSize + 2*x] = 
                    (p.inset == true)?InColor.GetPixelValue(p):
                    OutColor.GetPixelValue(p);
                p = MandelbrotinnerLoop(CReal[2*x+1], CImaginary[2*y+1]);
                pixels[(2*y+1)*XCanvasSize + 2*x+1] = 
                    (p.inset == true)?InColor.GetPixelValue(p):
                    OutColor.GetPixelValue(p);
                // First and Last Rows
                if ((y == 0) || (y == (YCanvasSize/2 - 1))){
                    p = MandelbrotinnerLoop(CReal[2*x+1], CImaginary[2*y]);
                    pixels[2*y*XCanvasSize + (2*x+1)] = 
                        (p.inset == true)?InColor.GetPixelValue(p):
                        OutColor.GetPixelValue(p);
                }
                // First and Last Columns
                if ((x == 0) || (x == (XCanvasSize/2 - 1))) {
                    p = MandelbrotinnerLoop(CReal[2*x], CImaginary[2*y+1]);
                    pixels[(2*y+1)*XCanvasSize + 2*x] = 
                        (p.inset == true)?InColor.GetPixelValue(p):
                        OutColor.GetPixelValue(p);
                }
            }
        }

        // Fill the holes in the grid with solid guessing
	for (int y = 0; y < YCanvasSize/2; y++) {
	    for (int x = 0; x < XCanvasSize/2; x++) {
                if ((y == 0) || (y == (YCanvasSize/2 - 1)) ||
                    (x == 0) || (x == (XCanvasSize/2 - 1))) {
                }
                else {
                    int N = pixels[(2*y - 1) *XCanvasSize + 2*x+1];
                    int S = pixels[(2*y +1) *XCanvasSize + 2*x+1];
                    int E = pixels[2*y*XCanvasSize + 2*x];
                    int W = pixels[2*y*XCanvasSize + 2*x+2];
                    if ((N == S) && (E == W) && (N == E)) {
                        pixels[2*y*XCanvasSize + 2*x+1] = N;
                    }
                    else {
                        PixelProperty p = MandelbrotinnerLoop(CReal[2*x+1], CImaginary[2*y]);
                        pixels[2*y*XCanvasSize + 2*x+1] = 
                            (p.inset == true)?InColor.GetPixelValue(p):
                            OutColor.GetPixelValue(p);
                    }
                    N = pixels[(2*y)*XCanvasSize + 2*x];
                    S = pixels[(2*y+2)*XCanvasSize + 2*x];
                    E = pixels[(2*y+1)*XCanvasSize + 2*x-1];
                    W = pixels[(2*y+1)*XCanvasSize + 2*x+1];
                    if ((N == S) && (E == W) && (N == E)) {
                        pixels[(2*y+1)*XCanvasSize + 2*x] = N;
                    }
                    else {
                        PixelProperty p = MandelbrotinnerLoop(CReal[2*x], CImaginary[2*y+1]);
                        pixels[(2*y+1)*XCanvasSize + 2*x] = 
                            (p.inset == true)?InColor.GetPixelValue(p):
                            OutColor.GetPixelValue(p);
                    }
                }
            }
        }
    }
    
    public void ComputeWithSolidGuessing3() {

        float[] CImaginary = new float[YCanvasSize];
        float[] CReal = new float[XCanvasSize];

        // Precompute Real component and imaginary component
	for (int y = 0; y < YCanvasSize; y++) {
            CImaginary[y] = YMin + y * Step;
        }

        for (int x = 0; x < XCanvasSize; x++) {
            CReal[x] = XMin + x * Step;
        }
        
	for (int y = 0; y < YCanvasSize/2; y++) {
	    for (int x = 0; x < XCanvasSize; x++) {
                PixelProperty p = MandelbrotinnerLoop(CReal[x], CImaginary[2*y]);
                pixels[2*y*XCanvasSize + x] = 
                    (p.inset == true)?InColor.GetPixelValue(p):
                    OutColor.GetPixelValue(p);
                if ((x == 0) || (y == (YCanvasSize/2 - 1))) {
                    p = MandelbrotinnerLoop(CReal[x], CImaginary[2*y+1]);
                    pixels[(2*y+1)*XCanvasSize + x] = 
                        (p.inset == true)?InColor.GetPixelValue(p):
                        OutColor.GetPixelValue(p);
                }
            }
        }
        
	for (int y = 0; y < YCanvasSize/2; y++) {
	    for (int x = 0; x < XCanvasSize; x++) {
                if ((x == 0) || (y == (YCanvasSize/2 - 1))) {

                }
                else {
                    int N = pixels[(2*y)*XCanvasSize + x];
                    int S = pixels[(2*y+2)*XCanvasSize + x];
                    int E = pixels[(2*y+1)*XCanvasSize + x-1];
                    if ((N == S) && (E == N)) {
                        pixels[(2*y+1)* XCanvasSize + x] =  N;
                    }
                    else {
                        PixelProperty p = MandelbrotinnerLoop(CReal[x], CImaginary[2*y+1]);
                        pixels[(2*y+1)*XCanvasSize + x] = 
                            (p.inset == true)?InColor.GetPixelValue(p):
                            OutColor.GetPixelValue(p);
                    }
                }
            }
        }
    }
    
    public void ComputeByPixelMoving() {

        float[] CImaginary = new float[YCanvasSize];
        float[] CReal = new float[XCanvasSize];

        // Precompute Real component and imaginary component
	for (int y = 0; y < YCanvasSize; y++) {
            CImaginary[y] = YMin + y * Step;
        }

        for (int x = 0; x < XCanvasSize; x++) {
            CReal[x] = XMin + x * Step;
        }

        boolean[][] CurrentPixelCopied = new boolean[XCanvasSize][YCanvasSize];
        float[][] CurrentXVal = new float[XCanvasSize][YCanvasSize];
        float[][] CurrentYVal = new float[XCanvasSize][YCanvasSize];
        
        int copied = 0;
        int created = 0;

        if ((PrevXVal != null) && (PrevYVal != null)) {
            int DeltaX = (int) ((PrevXMin-XMin)/Step);
            int DeltaY = (int) ((PrevYMin-YMin)/Step);
            float StepSQ = Step*Step;
            float StepRatio = PrevStep/Step;

            int OffsetX = (int) ((XMin - PrevXMin)/PrevStep);
            int OffsetY = (int) ((YMin - PrevYMin)/PrevStep);
            OffsetX = (OffsetX < 0)?0:OffsetX;
            OffsetY = (OffsetY < 0)?0:OffsetY;

            for (int y = OffsetY; y < YCanvasSize - OffsetY; y++) {
                int cy = (int) (DeltaY + y * StepRatio);
                for (int x = OffsetX; x < XCanvasSize - OffsetX; x++) {
                    int cx = (int) (DeltaX + x * StepRatio);
                    //System.out.println("CX and CY are " + cx + " " + cy); 
                    if (((0 <= cx) && (cx < XCanvasSize)) &&
                        ((0 <= cy) && (cy < YCanvasSize))){
                        if ( DistanceSQ(PrevXVal[x][y], PrevYVal[x][y], 
                                        CReal[cx], CImaginary[cy]) < StepSQ) {
                            if (CurrentPixelCopied[cx][cy] == false) {
                                CurrentPixelCopied[cx][cy] = true;
                                newpixels[cx][cy] = 
                                    pixels[y*XCanvasSize + x];
                                CurrentXVal[cx][cy] = PrevXVal[x][y];
                                CurrentYVal[cx][cy] = PrevYVal[x][y];
                                copied++;
                            }
                        }
                    }
                }
            }
            //System.out.println("DeltaX, DeltaY and StepRatio are " + DeltaX + 
            //                   " " + DeltaY + " " + StepRatio); 
            //System.out.println("Number of cells copied is: " + copied); 
        }


	for (int y = 0; y < YCanvasSize; y++) {
	    for (int x = 0; x < XCanvasSize; x++) {
                int index = y*XCanvasSize + x;
                if (CurrentPixelCopied[x][y] == false) {
                    PixelProperty p = MandelbrotinnerLoop(CReal[x], CImaginary[y]);
                    pixels[index] = 
                        (p.inset == true)?InColor.GetPixelValue(p):
                        OutColor.GetPixelValue(p);
                    CurrentXVal[x][y] = CReal[x];
                    CurrentYVal[x][y] = CImaginary[y];
                    created++;
                }
                else {
                    pixels[index] = newpixels[x][y];
                }
            }
        }


        //System.out.println("Number of cells newly created: " + created); 
        //System.out.println("Number of cells processed " + (created+copied)); 

        //System.arraycopy(newpixels, 0, pixels, 0, XCanvasSize*YCanvasSize);
        //pixels = newpixels;

        PrevXMin = XMin;
        PrevYMin = YMin;
        PrevStep = Step;
        PrevXVal = CurrentXVal;
        PrevYVal = CurrentYVal;
    }

    public void ComputeByPixelMovingAndSolidGuessing3() {

        float[] CImaginary = new float[YCanvasSize];
        float[] CReal = new float[XCanvasSize];

        // Precompute Real component and imaginary component
	for (int y = 0; y < YCanvasSize; y++) {
            CImaginary[y] = YMin + y * Step;
        }

        for (int x = 0; x < XCanvasSize; x++) {
            CReal[x] = XMin + x * Step;
        }

        boolean[][] CurrentPixelCopied = new boolean[XCanvasSize][YCanvasSize];
        float[][] CurrentXVal = new float[XCanvasSize][YCanvasSize];
        float[][] CurrentYVal = new float[XCanvasSize][YCanvasSize];
        
        //int copied = 0;
        //int created = 0;

        //long tm = System.currentTimeMillis();

        if ((PrevXVal != null) && (PrevYVal != null)) {
            int DeltaX = (int) ((PrevXMin-XMin)/Step);
            int DeltaY = (int) ((PrevYMin-YMin)/Step);
            float StepSQ = Step*Step;
            float StepRatio = PrevStep/Step;

            for (int y = 0; y < YCanvasSize; y++) {
                int cy = (int) (DeltaY + y * StepRatio);
                for (int x = 0; x < XCanvasSize; x++) {
                    int cx = (int) (DeltaX + x * StepRatio);
                    //System.out.println("CX and CY are " + cx + " " + cy); 
                    if (((0 <= cx) && (cx < XCanvasSize)) &&
                        ((0 <= cy) && (cy < YCanvasSize))){
                        if ( DistanceSQ(PrevXVal[x][y], PrevYVal[x][y], 
                                          CReal[cx], CImaginary[cy]) < 4 * StepSQ) {
                            if (CurrentPixelCopied[cx][cy] == false) {
                                CurrentPixelCopied[cx][cy] = true;
                                newpixels[cx][cy] = 
                                    pixels[y*XCanvasSize + x];
                                CurrentXVal[cx][cy] = PrevXVal[x][y];
                                CurrentYVal[cx][cy] = PrevYVal[x][y];
                                //copied++;
                            }
                       }
                    }
                }
            }
            //System.out.println("DeltaX, DeltaY and StepRatio are " + DeltaX + 
            //                   " " + DeltaY + " " + StepRatio); 
            //:System.out.println("Number of cells copied is " + copied); 
        }

        //:System.out.println("Time delat to copy is " + (System.currentTimeMillis() - tm));
        //tm = System.currentTimeMillis();


	for (int y = 0; y < YCanvasSize/2; y++) {
	    for (int x = 0; x < XCanvasSize; x++) {
                int index = 2*y*XCanvasSize + x;
                if (CurrentPixelCopied[x][2*y] == false) {
                    PixelProperty p = MandelbrotinnerLoop(CReal[x], CImaginary[2*y]);
                    pixels[index] = 
                        (p.inset == true)?InColor.GetPixelValue(p):
                        OutColor.GetPixelValue(p);
                    CurrentXVal[x][2*y] = CReal[x];
                    CurrentYVal[x][2*y] = CImaginary[2*y];
                    //created++;
                }
                else {
                    pixels[index] = newpixels[x][2*y];
                }
                if ((x == 0) || (y == (YCanvasSize/2 - 1))) {
                    index = (2*y+1)*XCanvasSize + x;
                    if (CurrentPixelCopied[x][2*y+1] == false) {
                        PixelProperty p = MandelbrotinnerLoop(CReal[x], CImaginary[2*y+1]);
                        pixels[index] = 
                            (p.inset == true)?InColor.GetPixelValue(p):
                            OutColor.GetPixelValue(p);
                        CurrentXVal[x][2*y+1] = CReal[x];
                        CurrentYVal[x][2*y+1] = CImaginary[2*y+1];
                        //created++;
                    }
                    else {
                        pixels[index] = newpixels[x][2*y+1];
                    }
                }
            }
        }

        //:System.out.println("Number of cells newly created in pass 1: " + created); 
        //:System.out.println("Time delat to create odd rows " + (System.currentTimeMillis() - tm));
        //tm = System.currentTimeMillis();

        //created = 0;
        //int guessed = 0;
	for (int y = 0; y < YCanvasSize/2; y++) {
	    for (int x = 0; x < XCanvasSize; x++) {
                if ((x == 0) || (y == (YCanvasSize/2 - 1))) {

                }
                else {
                    if (CurrentPixelCopied[x][2*y+1] == false) {
                        int N = pixels[(2*y)*XCanvasSize + x];
                        int S = pixels[(2*y+2)*XCanvasSize + x];
                        int E = pixels[(2*y+1)*XCanvasSize + x-1];
                        if ((N == S) && (E == N)) {
                            pixels[(2*y+1)* XCanvasSize + x] =  N;
                            //guessed++;
                        }
                        else {
                            PixelProperty p = MandelbrotinnerLoop(CReal[x], CImaginary[2*y+1]);
                            pixels[(2*y+1)*XCanvasSize + x] = 
                                (p.inset == true)?InColor.GetPixelValue(p):
                                OutColor.GetPixelValue(p);
                            //created++;
                        }
                        CurrentXVal[x][2*y+1] = CReal[x];
                        CurrentYVal[x][2*y+1] = CImaginary[2*y+1];
                    }
                    else {
                        pixels[(2*y+1)* XCanvasSize + x] = newpixels[x][2*y+1];
                    }
                }
            }
        }

        //:System.out.println("Number of cells guessed: " + guessed); 
        //:System.out.println("Number of cells newly created in pass 2: " + created); 

        //:System.out.println("Time delat to guess and create even rows " + 
        //                    (System.currentTimeMillis() - tm));
        //System.arraycopy(newpixels, 0, pixels, 0, XCanvasSize*YCanvasSize);
        //pixels = newpixels;

        PrevXMin = XMin;
        PrevYMin = YMin;
        PrevStep = Step;
        PrevXVal = CurrentXVal;
        PrevYVal = CurrentYVal;
    }

    float DistanceSQ(float a, float b, float c, float d) {
        return((a-c)*(a-c) + (b-d)*(b-d));
    }

    PixelProperty MandelbrotinnerLoop1(float cr, float ci) {

        PixelProperty p = new PixelProperty();
        int count;
                
        float zr = cr;
        float zi = ci;
        
        float zrsquared = zr * zr;
        float zisquared = zi * zi;
        
        for (count = 1; zrsquared + zisquared <= 4.0
                 && count < MaxIters; count++) {
            zi = zr * zi * 2 + ci;
            zr = zrsquared - zisquared + cr;
            
            zrsquared = zr * zr;
            zisquared = zi * zi;
        }

        p.inset = (zrsquared + zisquared <= 4.0)?true:false;
        p.iteration = count;
        p.real = zr;
        p.imaginary = zi;

        return p;
    }

    PixelProperty MandelbrotinnerLoop(float cr, float ci) {

        PixelProperty p = new PixelProperty();
        int count;
        int NUM_FRAC_BITS = 28;
        int F4 = 4 << NUM_FRAC_BITS;

        long Fcr = (long)(cr * (1 << NUM_FRAC_BITS)); 
        long Fci = (long)(ci * (1 << NUM_FRAC_BITS)); 
                
        long Fzr = Fcr;
        long Fzi = Fci;
        
        long Fzrsquared = (Fzr * Fzr) >> NUM_FRAC_BITS;
        long Fzisquared = (Fzi * Fzi) >> NUM_FRAC_BITS;
        
        for (count = 1; Fzrsquared + Fzisquared <= F4
                 && count < MaxIters; count++) {
            Fzi = ((Fzr * Fzi) >> (NUM_FRAC_BITS - 1)) + Fci;
            Fzr = Fzrsquared - Fzisquared + Fcr;
            
            Fzrsquared = (Fzr * Fzr) >> NUM_FRAC_BITS;
            Fzisquared = (Fzi * Fzi) >> NUM_FRAC_BITS;
        }

        p.inset = (Fzrsquared + Fzisquared <= F4)?true:false;
        p.iteration = count;
        p.real = (float)(Fzr)/(float)(1 << NUM_FRAC_BITS); 
        p.imaginary = (float)(Fzi)/(float)(1 << NUM_FRAC_BITS); 

        return p;
    }
}

class PixelProperty {
    boolean inset;
    int iteration;
    float real;
    float imaginary;
}

class OutColor {
    static int Mode = 3;
    private static final int BLACK = 0xff000000;
    private static int sw = (int)(Math.random()* 255);
    private static int sr = (int)(Math.random()* 255);
    private static int sg = (int)(Math.random()* 255);
    private static int sb = (int)(Math.random()* 255);

    private static RandomPalette RandPalette = new RandomPalette(171);
    private static int[] RPalette = RandPalette.GetPalette();

    static int GetPixelValue(PixelProperty p) {
        int pixel = BLACK;
        switch (Mode) {
        case 0:
            int w = (int) ((Math.sin (p.imaginary / p.real)) * 127);
            int r = (int) ((int) (p.real * p.imaginary));
            int g = (int) ((Math.sin ( (p.real * p.real) / 2) + 1) * 127);
            int b = (int) ((Math.sin ((float) Math.atan2 (p.real, p.imaginary) * 20) 
                            + 1) * 127);
            pixel = w << 24 | r << 16 | g << 8 | b;
            break;
        case 1:
            pixel = sw << 24 | sr << 16 | p.iteration << 8 | sb;
            break;
            
        case  2:
            int index = p.iteration % 32;
            b = (int)  ColorPalette.colors1[index][2];
            g = (int)  ColorPalette.colors1[index][1];
            r = (int)  ColorPalette.colors1[index][0];

            pixel = 200 << 24 | r << 16 | g << 8 | b;
            break;
        case  3:
            pixel = RPalette[p.iteration];
            break;
        default: 
            System.out.println("Need to throw an exception");
        }
        return (pixel);
    }
}

class InColor {
    private static final int BLACK = 0xff000000;
    static int Mode = 0;
    static int GetPixelValue(PixelProperty p) {
        int pixel = BLACK;
        switch (Mode) {
        case 0:
            pixel = BLACK;
            break;
        default: 
            System.out.println("Need to throw an exception");
        }
        return (pixel);
    }
}

class ColorPalette {
static final int[][] colors1 = {
  {0, 0, 0},
  {8, 14, 32}, 
  {120, 119, 238},
  {24, 7, 25},
  {197, 66, 28},
  {29, 18, 11},
  {135, 46, 71},
  {24, 27, 13},
  {241, 230, 128},
  {17, 31, 24},
  {240, 162, 139},
  {11, 4, 30},
  {106, 87, 189},
  {29, 21, 14},
  {12, 140, 118},
  {10, 6, 29},
  {50, 144, 77},
  {22, 0, 24},
  {148, 188, 243},
  {4, 32, 7},
  {231, 146, 14},
  {10, 13, 20},
  {184, 147, 68},
  {13, 28, 3},
  {169, 248, 152},
  {4, 0, 34},
  {62, 83, 48},
  {7, 21, 22},
  {152, 97, 184},
  {8, 3, 12},
  {247, 92, 235},
  {31, 32, 16}
};

}


class RandomPalette {

    private static int[] Palette;
    private static int woof = 0xf0;

    RandomPalette (int size) {
        Palette = new int[size];
        for (int i = 0; i < size; i++) {
            Palette[i] = (int)(Math.random()* 0x0f + woof) << 24 | 
                         (int)(Math.random()* 0xff ) << 16 |
                         (int)(Math.random()* 0xff ) << 8  | 
                         (int)(Math.random()* 0xff );
        }
        //return Palette;
    }
    static int[] GetPalette () {
        return Palette;
    }
}
