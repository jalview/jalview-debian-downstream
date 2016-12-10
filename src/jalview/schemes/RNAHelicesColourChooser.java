/*
 * Jalview - A Sequence Alignment Editor and Viewer (2.10.1)
 * Copyright (C) 2016 The Jalview Authors
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
package jalview.schemes;

import jalview.api.AlignViewportI;
import jalview.api.AlignmentViewPanel;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.SequenceGroup;

import java.awt.event.ActionEvent;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Helps generate the colors for RNA secondary structure. Future: add option to
 * change colors based on covariation.
 * 
 * @author Lauren Michelle Lui
 * 
 */
public class RNAHelicesColourChooser
{

  AlignViewportI av;

  AlignmentViewPanel ap;

  ColourSchemeI oldcs;

  Hashtable oldgroupColours;

  jalview.datamodel.AlignmentAnnotation currentAnnotation;

  boolean adjusting = false;

  public RNAHelicesColourChooser(AlignViewportI av,
          final AlignmentViewPanel ap)
  {
    oldcs = av.getGlobalColourScheme();
    if (av.getAlignment().getGroups() != null)
    {
      oldgroupColours = new Hashtable();
      for (SequenceGroup sg : ap.getAlignment().getGroups())
      {
        if (sg.cs != null)
        {
          oldgroupColours.put(sg, sg.cs);
        }
      }
    }
    this.av = av;
    this.ap = ap;

    if (oldcs instanceof RNAHelicesColour)
    {
      RNAHelicesColour rhc = (RNAHelicesColour) oldcs;

    }

    adjusting = true;
    Vector list = new Vector();
    int index = 1;
    AlignmentAnnotation[] anns = av.getAlignment().getAlignmentAnnotation();
    if (anns != null)
    {
      for (int i = 0; i < anns.length; i++)
      {
        String label = anns[i].label;
        if (!list.contains(label))
        {
          list.addElement(label);
        }
        else
        {
          list.addElement(label + "_" + (index++));
        }
      }
    }

    adjusting = false;

    changeColour();

  }

  void changeColour()
  {
    // Check if combobox is still adjusting
    if (adjusting)
    {
      return;
    }
    RNAHelicesColour rhc = null;

    rhc = new RNAHelicesColour(av.getAlignment());

    av.setGlobalColourScheme(rhc);

    ap.paintAlignment(true);
  }

  void reset()
  {
    av.setGlobalColourScheme(oldcs);
    if (av.getAlignment().getGroups() != null)
    {
      for (SequenceGroup sg : ap.getAlignment().getGroups())
      {
        sg.cs = (ColourSchemeI) oldgroupColours.get(sg);
      }
    }
  }

  public void annotations_actionPerformed(ActionEvent e)
  {
    changeColour();
  }

}
