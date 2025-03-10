/*
 * Jalview - A Sequence Alignment Editor and Viewer (2.11.1.3)
 * Copyright (C) 2020 The Jalview Authors
 * 
 * This file is part of Jalview.
 * 
 * Jalview is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *  
 * Jalview is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR 
 * PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Jalview.  If not, see <http://www.gnu.org/licenses/>.
 * The Jalview Authors are detailed in the 'AUTHORS' file.
 */
package jalview.renderer;

import java.awt.FontMetrics;
import java.awt.Image;
import java.awt.image.ImageObserver;

/**
 * semi-insulated interface for rendering a faded image whilst a calculation is
 * in progress on annotation panel. Will need to remove java.awt dependencies
 * for Android/etc
 * 
 * @author jprocter
 *
 */
public interface AwtRenderPanelI extends ImageObserver
{
  /**
   * old image used when data is currently being calculated and cannot be
   * rendered
   */
  Image getFadedImage();

  /**
   * FontMetrics to use for rendering into Panel
   * 
   * @return
   */
  FontMetrics getFontMetrics();

  /**
   * width of image to render in panel
   */
  int getFadedImageWidth();

  /**
   * height of visible area on to the image - used to draw only what is visible.
   * 
   * @return [start, end of visible region]
   */
  int[] getVisibleVRange();

}
