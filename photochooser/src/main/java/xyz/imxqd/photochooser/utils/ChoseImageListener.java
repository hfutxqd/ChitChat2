/**
 *
 */
package xyz.imxqd.photochooser.utils;


import xyz.imxqd.photochooser.model.ImageBean;

/**
 * @author xiaolf1
 * updated by ywwynm
 */
public interface ChoseImageListener {

    public boolean onSelected(ImageBean image);

    public boolean onCancelSelect(ImageBean image);
}
