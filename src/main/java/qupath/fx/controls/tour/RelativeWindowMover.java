package qupath.fx.controls.tour;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.stage.Window;

import java.util.Objects;

/**
 * Helper class to move a window relative to another window.
 * <p>
 * On macOS, windows already move relative to their owner windows -
 * but this does not happen on Windows or Linux.
 * <p>
 * This class makes it possible to achieve similar behavior.
 * It also makes it possible to move a window relative to a different window
 * from its original owner.
 */
public class RelativeWindowMover {

    private Window currentOwner;
    private final Window window;

    private ChangeListener<Number> xListener = this::handleX;
    private ChangeListener<Number> yListener = this::handleY;

    /**
     * Constructor.
     * @param window the window that should be moved relative to another window
     */
    public RelativeWindowMover(Window window) {
        Objects.requireNonNull(window);
        this.window = window;
    }

    /**
     * Attach to an owner window.
     * @param owner the owner to use; when the owner moves, the current window
     *              should move by the same amount.
     */
    public void attach(Window owner) {
        Objects.requireNonNull(owner);
        detach();
        owner.xProperty().addListener(xListener);
        owner.yProperty().addListener(yListener);
        currentOwner = owner;
    }

    /**
     * Detach from the current owner window.
     */
    public void detach() {
        if (currentOwner != null) {
            currentOwner.xProperty().removeListener(xListener);
            currentOwner.yProperty().removeListener(yListener);
        }
    }

    /**
     * Get the current owner window, or null if there is no owner.
     * @return
     */
    public Window getOwner() {
        return currentOwner;
    }

    /**
     * Get the current window, which may be moved relative to an owner.
     * @return
     */
    public Window getWindow() {
        return window;
    }

    /**
     * Set the location of the managed window.
     * @param x new desired x location, in screen coordinates
     * @param y new desired y location, in screen coordinates
     */
    public void moveTo(double x, double y) {
        window.setX(x);
        window.setY(y);
    }

    private void handleX(ObservableValue<? extends Number> value, Number oldValue, Number newValue) {
        double deltaX = newValue.doubleValue() - oldValue.doubleValue();
        this.window.setX(this.window.getX() + deltaX);
    }

    private void handleY(ObservableValue<? extends Number> value, Number oldValue, Number newValue) {
        double deltaY = newValue.doubleValue() - oldValue.doubleValue();
        this.window.setY(this.window.getY() + deltaY);
    }

}
