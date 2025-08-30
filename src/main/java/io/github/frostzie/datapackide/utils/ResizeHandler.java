package io.github.frostzie.datapackide.utils;

/** Copyright © 2021 Izon Company, Free To Share: class ResizeHelper.java */
// https://stackoverflow.com/questions/19455059/allow-user-to-resize-an-undecorated-stage
// Slightly modified by me (Frostzie) to change curser style when resizing

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * handles Stage resizing for StageStyle.UNDECORATED and deals with a
 * indentation which can be used to render a CSS drop shadow effect around the
 * scene with transparent background.
 *
 * @author Henryk Zschuppan,
 * @date MARCH 01,21
 */

public class ResizeHandler implements EventHandler<MouseEvent> {

    public static ResizeHandler install(Stage stage, double topbarHeight, double pullEdgeDepth, double indentation) {
        ResizeHandler handler = new ResizeHandler(stage, topbarHeight, pullEdgeDepth, indentation);
        stage.getScene().addEventHandler(MouseEvent.ANY, handler);
        return handler;
    }

    public static Rectangle2D SCREEN_BOUNDS = Screen.getPrimary().getVisualBounds();

    /** select the boundary clipping orientation in relation to the stage */
    private static enum CHECK {
        LOW,
        HIGH,
        NONE
    }

    /** Stage to which the handler is implemented */
    final private Stage stage;
    /** Area from top to consider for stage reposition */
    double topbarHeight;
    /** Space to consider around the stage border for resizing */
    final private int depth;
    /** padding space to render in the CSS effect drop shadow */
    final private double pad;
    /** stage size limits */
    final private double minWidth, minHeight, maxWidth, maxHeight;
    /** start point of mouse position on screen */
    private Point2D startDrag = null;
    /** frame rectangle of the stage on drag start */
    private Rectangle2D startRectangle;
    /** the relative mouse orientation to the stage */
    private CHECK checkX = CHECK.NONE, checkY = CHECK.NONE;

    private boolean inRepositioningArea = false;

    private ResizeHandler(Stage stage, double topbarHeight, double pullEdgeDepth, double indentation) {
        this.stage = stage;
        this.topbarHeight = topbarHeight;
        pad = indentation;
        depth = (int) (indentation + pullEdgeDepth);

        minWidth = stage.getMinWidth();
        minHeight = stage.getMinHeight();
        maxWidth = stage.getMaxWidth();
        maxHeight = stage.getMaxHeight();
    }

    private boolean isStageMaximized() {
        for (Screen screen : Screen.getScreensForRectangle(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight())) {
            Rectangle2D visualBounds = screen.getVisualBounds();
            if (stage.getX() == visualBounds.getMinX() &&
                stage.getY() == visualBounds.getMinY() &&
                stage.getWidth() == visualBounds.getWidth() &&
                stage.getHeight() == visualBounds.getHeight()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void handle(MouseEvent mouseEvent) {
        EventType<? extends MouseEvent> mouseEventType = mouseEvent.getEventType();
        final double lX = mouseEvent.getSceneX();
        final double lY = mouseEvent.getSceneY();
        final double sW = stage.getWidth();
        final double sH = stage.getHeight();
        final boolean isMaximized = isStageMaximized();

        if (MouseEvent.MOUSE_MOVED.equals(mouseEventType)) {
            if (startDrag == null) {
                Cursor cursor = Cursor.DEFAULT;
                if (!isMaximized && lX > pad && lX < sW - pad && lY > pad && lY < sH - pad) {
                    if (lX < depth && lY < depth) cursor = Cursor.NW_RESIZE;
                    else if (lX < depth && lY > sH - depth) cursor = Cursor.SW_RESIZE;
                    else if (lX > sW - depth && lY < depth) cursor = Cursor.NE_RESIZE;
                    else if (lX > sW - depth && lY > sH - depth) cursor = Cursor.SE_RESIZE;
                    else if (lX < depth) cursor = Cursor.W_RESIZE;
                    else if (lX > sW - depth) cursor = Cursor.E_RESIZE;
                    else if (lY < depth) cursor = Cursor.N_RESIZE;
                    else if (lY > sH - depth) cursor = Cursor.S_RESIZE;
                }
                stage.getScene().setCursor(cursor);
            }
        } else if (MouseEvent.MOUSE_EXITED.equals(mouseEventType) || MouseEvent.MOUSE_EXITED_TARGET.equals(mouseEventType)) {
            if (startDrag == null) {
                stage.getScene().setCursor(Cursor.DEFAULT);
            }
        }

        if (!mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
            return;
        }

        final double mX = mouseEvent.getScreenX();
        final double mY = mouseEvent.getScreenY();

        if (MouseEvent.MOUSE_PRESSED.equals(mouseEventType)) {
            if (isMaximized) {
                setXYCheck(CHECK.NONE, CHECK.NONE);
            } else {
                if (lX < depth && lY < depth) {
                    setXYCheck(CHECK.LOW, CHECK.LOW);
                } else if (lX < depth && lY > sH - depth) {
                    setXYCheck(CHECK.LOW, CHECK.HIGH);
                } else if (lX > sW - depth && lY < depth) {
                    setXYCheck(CHECK.HIGH, CHECK.LOW);
                } else if (lX > sW - depth && lY > sH - depth) {
                    setXYCheck(CHECK.HIGH, CHECK.HIGH);
                } else if (lX < depth) {
                    setXYCheck(CHECK.LOW, CHECK.NONE);
                } else if (lX > sW - depth) {
                    setXYCheck(CHECK.HIGH, CHECK.NONE);
                } else if (lY < depth) {
                    setXYCheck(CHECK.NONE, CHECK.LOW);
                } else if (lY > sH - depth) {
                    setXYCheck(CHECK.NONE, CHECK.HIGH);
                } else {
                    setXYCheck(CHECK.NONE, CHECK.NONE);
                }

                /* check mouse is not inside the resize border space */
                if (lX < pad || lY < pad || lX > sW - pad || lY > sH - pad) {
                    setXYCheck(CHECK.NONE, CHECK.NONE);
                }
            }

            inRepositioningArea = lY >= depth && lY < this.topbarHeight + pad;

            startDrag = new Point2D(mX, mY);
            startRectangle = new Rectangle2D(stage.getX(), stage.getY(), sW, sH);

        } else if (!isNone() && MouseEvent.MOUSE_DRAGGED.equals(mouseEventType)) {
            /* stage resizing */
            double dX = mX - startDrag.getX();
            double dY = mY - startDrag.getY();
            double min, max;
            /* don't overwrite start values */
            double x = startRectangle.getMinX(), y = startRectangle.getMinY(), x2 = startRectangle.getMaxX(), y2 = startRectangle.getMaxY();

            switch (checkX) {
                case LOW :// LEFT
                    min = Math.max(x - maxWidth, (0 - pad));
                    max = x2 - minWidth;
                    x = Math.clamp(x + dX, min, max);
                    break;
                case HIGH : // RIGHT
                    min = x + minWidth;
                    max = Math.min(x + maxWidth, SCREEN_BOUNDS.getWidth() + pad);
                    x2 = Math.clamp(x2 + dX, min, max);
                default :
                    break;
            }

            switch (checkY) {
                case LOW : // TOP
                    min = Math.max(y2 - maxHeight, (0 - pad));
                    max = y2 - minHeight;
                    y = Math.clamp(y + dY, min, max);
                    break;
                case HIGH :// BOTTOM
                    min = y + minHeight;
                    max = Math.min(y + maxHeight, SCREEN_BOUNDS.getHeight() + pad);
                    y2 = Math.clamp(y2 + dY, min, max);
                default :
                    break;
            }

            updateStagePosition(x, y, x2, y2);

        } else if (isNone() && MouseEvent.MOUSE_DRAGGED.equals(mouseEventType) && inRepositioningArea) {
            /* stage repositioning */
            double dX = mX - startDrag.getX();
            double dY = mY - startDrag.getY();

            this.stage.setX(startRectangle.getMinX() + dX);
            this.stage.setY(startRectangle.getMinY() + dY);

        } else if (!isNone() && MouseEvent.MOUSE_RELEASED.equals(mouseEventType) && mouseEvent.getClickCount() == 2) {
            /* The stage side is expanded or minimized by double-clicking */
            double min, max;
            /* don't overwrite start values */
            double x = startRectangle.getMinX(), y = startRectangle.getMinY(), x2 = startRectangle.getMaxX(), y2 = startRectangle.getMaxY();

            switch (checkX) {
                case LOW :// LEFT
                    if (x > (0 - pad)) {
                        min = Math.max(x - maxWidth, (0 - pad));
                        max = x2 - minWidth;
                        x = Math.clamp((0 - pad), min, max);
                    } else {
                        x = x2 - minWidth;
                    }
                    break;
                case HIGH : // RIGHT
                    if (x2 < SCREEN_BOUNDS.getWidth() + pad) {
                        min = x + minWidth;
                        max = Math.min(x + maxWidth, SCREEN_BOUNDS.getWidth() + pad);
                        x2 = Math.clamp(SCREEN_BOUNDS.getWidth() + pad, min, max);
                    } else {
                        x2 = x + minWidth;
                    }
                default :
                    break;
            }

            switch (checkY) {
                case LOW : // TOP
                    if (y > (0 - pad)) {
                        min = Math.max(y2 - maxHeight, (0 - pad));
                        max = y2 - minHeight;
                        y = Math.clamp((0 - pad), min, max);
                    } else {
                        y = y2 - minHeight;
                    }
                    break;
                case HIGH :// BOTTOM
                    if (y2 < SCREEN_BOUNDS.getHeight() + pad) {
                        min = y + minHeight;
                        max = Math.min(y + maxHeight, SCREEN_BOUNDS.getHeight() + pad);
                        y2 = Math.clamp(SCREEN_BOUNDS.getHeight() + pad, min, max);
                    } else {
                        y2 = y + minHeight;
                    }
                default :
                    break;
            }

            updateStagePosition(x, y, x2, y2);
        } else if (MouseEvent.MOUSE_RELEASED.equals(mouseEventType)) {
            startDrag = null; // End of drag
            // The cursor will be updated on the next MOUSE_MOVED event
        }
    }

    private void setXYCheck(CHECK X, CHECK Y) {
        checkX = X;
        checkY = Y;
    }

    /** @return true if checkX and checkY is set to CHECK.NONE */
    private boolean isNone() {
        return checkX.equals(CHECK.NONE) && checkY.equals(CHECK.NONE);
    }

    private void updateStagePosition(double x1, double y1, double x2, double y2) {
        stage.setX(x1);
        stage.setY(y1);
        stage.setWidth(x2 - x1);
        stage.setHeight(y2 - y1);
    }
} // CLASS END
