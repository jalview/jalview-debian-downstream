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
package jalview.gui;

import jalview.api.structures.JalviewStructureDisplayI;
import jalview.datamodel.SequenceGroup;
import jalview.io.JalviewFileChooser;
import jalview.jbgui.GUserDefinedColours;
import jalview.schemes.ColourSchemeI;
import jalview.schemes.ResidueProperties;
import jalview.schemes.UserColourScheme;
import jalview.util.ColorUtils;
import jalview.util.MessageManager;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * This panel allows the user to assign colours to Amino Acid residue codes, and
 * save the colour scheme.
 * 
 * @author Andrew Waterhouse
 * @author Mungo Carstairs
 */
public class UserDefinedColours extends GUserDefinedColours implements
        ChangeListener
{
  private static final int MY_FRAME_HEIGHT = 420;

  private static final int MY_FRAME_WIDTH = 810;

  private static final int MY_FRAME_WIDTH_CASE_SENSITIVE = 970;

  AlignmentPanel ap;

  SequenceGroup seqGroup;

  ArrayList<JButton> selectedButtons;

  ColourSchemeI oldColourScheme;

  JInternalFrame frame;

  JalviewStructureDisplayI jmol;

  ArrayList<JButton> upperCaseButtons;

  ArrayList<JButton> lowerCaseButtons;

  /**
   * Creates a new UserDefinedColours object.
   * 
   * @param ap
   *          DOCUMENT ME!
   * @param sg
   *          DOCUMENT ME!
   */
  public UserDefinedColours(AlignmentPanel ap, SequenceGroup sg)
  {
    super();

    lcaseColour.setEnabled(false);

    this.ap = ap;
    seqGroup = sg;

    if (seqGroup != null)
    {
      oldColourScheme = seqGroup.cs;
    }
    else
    {
      oldColourScheme = ap.av.getGlobalColourScheme();
    }

    if (oldColourScheme instanceof UserColourScheme)
    {
      schemeName.setText(((UserColourScheme) oldColourScheme).getName());
      if (((UserColourScheme) oldColourScheme).getLowerCaseColours() != null)
      {
        caseSensitive.setSelected(true);
        lcaseColour.setEnabled(true);
        resetButtonPanel(true);
      }
      else
      {
        resetButtonPanel(false);
      }
    }
    else
    {
      resetButtonPanel(false);
    }

    showFrame();
  }

  public UserDefinedColours(JalviewStructureDisplayI jmol,
          ColourSchemeI oldcs)
  {
    super();
    this.jmol = jmol;

    colorChooser.getSelectionModel().addChangeListener(this);

    oldColourScheme = oldcs;

    if (oldColourScheme instanceof UserColourScheme)
    {
      schemeName.setText(((UserColourScheme) oldColourScheme).getName());
    }

    resetButtonPanel(false);

    showFrame();

  }

  void showFrame()
  {
    colorChooser.getSelectionModel().addChangeListener(this);
    frame = new JInternalFrame();
    frame.setContentPane(this);
    Desktop.addInternalFrame(frame,
            MessageManager.getString("label.user_defined_colours"),
            MY_FRAME_WIDTH, MY_FRAME_HEIGHT, true);

    if (seqGroup != null)
    {
      frame.setTitle(frame.getTitle() + " (" + seqGroup.getName() + ")");
    }
  }

  void resetButtonPanel(boolean caseSensitive)
  {
    buttonPanel.removeAll();

    if (upperCaseButtons == null)
    {
      upperCaseButtons = new ArrayList<JButton>();
    }

    JButton button;
    String label;
    for (int i = 0; i < 20; i++)
    {
      if (caseSensitive)
      {
        label = ResidueProperties.aa[i];
      }
      else
      {
        label = ResidueProperties.aa2Triplet.get(ResidueProperties.aa[i])
                .toString();
      }

      button = makeButton(label, ResidueProperties.aa[i], upperCaseButtons,
              i);

      buttonPanel.add(button);
    }

    buttonPanel.add(makeButton("B", "B", upperCaseButtons, 20));
    buttonPanel.add(makeButton("Z", "Z", upperCaseButtons, 21));
    buttonPanel.add(makeButton("X", "X", upperCaseButtons, 22));
    buttonPanel.add(makeButton("Gap", "-", upperCaseButtons, 23));

    if (!caseSensitive)
    {
      gridLayout.setRows(6);
      gridLayout.setColumns(4);
    }
    else
    {
      gridLayout.setRows(7);
      int cols = 7;
      gridLayout.setColumns(cols + 1);

      if (lowerCaseButtons == null)
      {
        lowerCaseButtons = new ArrayList<JButton>();
      }

      for (int i = 0; i < 20; i++)
      {
        int row = i / cols + 1;
        int index = (row * cols) + i;
        button = makeButton(ResidueProperties.aa[i].toLowerCase(),
                ResidueProperties.aa[i].toLowerCase(), lowerCaseButtons, i);

        buttonPanel.add(button, index);
      }
    }

    if (caseSensitive)
    {
      buttonPanel.add(makeButton("b", "b", lowerCaseButtons, 20));
      buttonPanel.add(makeButton("z", "z", lowerCaseButtons, 21));
      buttonPanel.add(makeButton("x", "x", lowerCaseButtons, 22));
    }

    // JAL-1360 widen the frame dynamically to accommodate case-sensitive AA
    // codes
    if (this.frame != null)
    {
      int newWidth = caseSensitive ? MY_FRAME_WIDTH_CASE_SENSITIVE
              : MY_FRAME_WIDTH;
      this.frame.setSize(newWidth, this.frame.getHeight());
    }

    buttonPanel.validate();
    validate();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param evt
   *          DOCUMENT ME!
   */
  @Override
  public void stateChanged(ChangeEvent evt)
  {
    if (selectedButtons != null)
    {
      JButton button = null;
      final Color newColour = colorChooser.getColor();
      for (int i = 0; i < selectedButtons.size(); i++)
      {
        button = selectedButtons.get(i);
        button.setBackground(newColour);
        button.setForeground(ColorUtils.brighterThan(newColour));
      }
      if (button == lcaseColour)
      {
        for (int i = 0; i < lowerCaseButtons.size(); i++)
        {
          button = lowerCaseButtons.get(i);
          button.setBackground(newColour);
          button.setForeground(ColorUtils.brighterThan(button
                  .getBackground()));
        }
      }
    }
  }

  /**
   * Performs actions when a residue button is clicked. This manages the button
   * selection set (highlighted by brighter foreground text).
   * <p>
   * On select button(s) with Ctrl/click or Shift/click: set button foreground
   * text to brighter than background.
   * <p>
   * On unselect button(s) with Ctrl/click on selected, or click to release
   * current selection: reset foreground text to darker than background.
   * <p>
   * Simple click: clear selection (resetting foreground to darker); set clicked
   * button foreground to brighter
   * <p>
   * Finally, synchronize the colour chooser to the colour of the first button
   * in the selected set.
   * 
   * @param e
   */
  public void colourButtonPressed(MouseEvent e)
  {
    if (selectedButtons == null)
    {
      selectedButtons = new ArrayList<JButton>();
    }

    JButton pressed = (JButton) e.getSource();

    if (e.isShiftDown())
    {
      JButton start, end = (JButton) e.getSource();
      if (selectedButtons.size() > 0)
      {
        start = selectedButtons.get(selectedButtons.size() - 1);
      }
      else
      {
        start = (JButton) e.getSource();
      }

      int startIndex = 0, endIndex = 0;
      for (int b = 0; b < buttonPanel.getComponentCount(); b++)
      {
        if (buttonPanel.getComponent(b) == start)
        {
          startIndex = b;
        }
        if (buttonPanel.getComponent(b) == end)
        {
          endIndex = b;
        }
      }

      if (startIndex > endIndex)
      {
        int temp = startIndex;
        startIndex = endIndex;
        endIndex = temp;
      }

      for (int b = startIndex; b <= endIndex; b++)
      {
        JButton button = (JButton) buttonPanel.getComponent(b);
        if (!selectedButtons.contains(button))
        {
          button.setForeground(ColorUtils.brighterThan(button
                  .getBackground()));
          selectedButtons.add(button);
        }
      }
    }
    else if (!e.isControlDown())
    {
      for (int b = 0; b < selectedButtons.size(); b++)
      {
        JButton button = selectedButtons.get(b);
        button.setForeground(ColorUtils.darkerThan(button.getBackground()));
      }
      selectedButtons.clear();
      pressed.setForeground(ColorUtils.brighterThan(pressed.getBackground()));
      selectedButtons.add(pressed);

    }
    else if (e.isControlDown())
    {
      if (selectedButtons.contains(pressed))
      {
        pressed.setForeground(ColorUtils.darkerThan(pressed.getBackground()));
        selectedButtons.remove(pressed);
      }
      else
      {
        pressed.setForeground(ColorUtils.brighterThan(pressed
                .getBackground()));
        selectedButtons.add(pressed);
      }
    }

    if (selectedButtons.size() > 0)
    {
      colorChooser.setColor((selectedButtons.get(0)).getBackground());
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param label
   *          DOCUMENT ME!
   * @param aa
   *          DOCUMENT ME!
   */
  JButton makeButton(String label, String aa,
          ArrayList<JButton> caseSensitiveButtons, int buttonIndex)
  {
    final JButton button;
    Color col;

    if (buttonIndex < caseSensitiveButtons.size())
    {
      button = caseSensitiveButtons.get(buttonIndex);
      col = button.getBackground();
    }
    else
    {
      button = new JButton();
      button.addMouseListener(new java.awt.event.MouseAdapter()
      {
        @Override
        public void mouseClicked(MouseEvent e)
        {
          colourButtonPressed(e);
        }
      });

      caseSensitiveButtons.add(button);

      col = Color.white;
      if (oldColourScheme != null)
      {
        try
        {
          col = oldColourScheme.findColour(aa.charAt(0), -1, null);
        } catch (Exception ex)
        {
        }
      }
    }

    if (caseSensitive.isSelected())
    {
      button.setMargin(new java.awt.Insets(2, 2, 2, 2));
    }
    else
    {
      button.setMargin(new java.awt.Insets(2, 14, 2, 14));
    }

    button.setOpaque(true); // required for the next line to have effect
    button.setBackground(col);
    button.setText(label);
    button.setForeground(ColorUtils.darkerThan(col));
    button.setFont(new java.awt.Font("Verdana", Font.BOLD, 10));

    return button;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  protected void okButton_actionPerformed(ActionEvent e)
  {
    if (isNoSelectionMade())
    {
      JOptionPane.showMessageDialog(Desktop.desktop, MessageManager
              .getString("label.no_colour_selection_in_scheme"),
              MessageManager.getString("label.no_colour_selection_warn"),
              JOptionPane.WARNING_MESSAGE);
    }
    else
    {
      applyButton_actionPerformed(null);

      try
      {
        frame.setClosed(true);
      } catch (Exception ex)
      {
      }
    }
  }

  /**
   * Returns true if the user has not made any colour selection (including if
   * 'case-sensitive' selected and no lower-case colour chosen).
   * 
   * @return
   */
  protected boolean isNoSelectionMade()
  {
    final boolean noUpperCaseSelected = upperCaseButtons == null
            || upperCaseButtons.isEmpty();
    final boolean noLowerCaseSelected = caseSensitive.isSelected()
            && (lowerCaseButtons == null || lowerCaseButtons.isEmpty());
    final boolean noSelectionMade = noUpperCaseSelected
            || noLowerCaseSelected;
    return noSelectionMade;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  protected void applyButton_actionPerformed(ActionEvent e)
  {
    if (isNoSelectionMade())
    {
      JOptionPane.showMessageDialog(Desktop.desktop, MessageManager
              .getString("label.no_colour_selection_in_scheme"),
              MessageManager.getString("label.no_colour_selection_warn"),
              JOptionPane.WARNING_MESSAGE);

    }
    UserColourScheme ucs = getSchemeFromButtons();
    ucs.setName(schemeName.getText());

    if (seqGroup != null)
    {
      seqGroup.cs = ucs;
      ap.paintAlignment(true);
    }
    else if (ap != null)
    {
      ap.alignFrame.changeColour(ucs);
    }
    else if (jmol != null)
    {
      jmol.setJalviewColourScheme(ucs);
    }
  }

  UserColourScheme getSchemeFromButtons()
  {

    Color[] newColours = new Color[24];

    int length = upperCaseButtons.size();
    if (length < 24)
    {
      int i = 0;
      for (JButton btn : upperCaseButtons)
      {
        newColours[i] = btn.getBackground();
        i++;
      }
    }
    else
    {
      for (int i = 0; i < 24; i++)
      {
        JButton button = upperCaseButtons.get(i);
        newColours[i] = button.getBackground();
      }
    }

    UserColourScheme ucs = new UserColourScheme(newColours);

    if (caseSensitive.isSelected())
    {
      newColours = new Color[23];
      length = lowerCaseButtons.size();
      if (length < 23)
      {
        int i = 0;
        for (JButton btn : lowerCaseButtons)
        {
          newColours[i] = btn.getBackground();
          i++;
        }
      }
      else
      {
        for (int i = 0; i < 23; i++)
        {
          JButton button = lowerCaseButtons.get(i);
          newColours[i] = button.getBackground();
        }
      }
      ucs.setLowerCaseColours(newColours);
    }

    if (ap != null)
    {
      ucs.setThreshold(0, ap.av.isIgnoreGapsConsensus());
    }

    return ucs;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  protected void loadbutton_actionPerformed(ActionEvent e)
  {
    upperCaseButtons = new ArrayList<JButton>();
    lowerCaseButtons = new ArrayList<JButton>();

    JalviewFileChooser chooser = new JalviewFileChooser(
            jalview.bin.Cache.getProperty("LAST_DIRECTORY"),
            new String[] { "jc" }, new String[] { "Jalview User Colours" },
            "Jalview User Colours");
    chooser.setFileView(new jalview.io.JalviewFileView());
    chooser.setDialogTitle(MessageManager
            .getString("label.load_colour_scheme"));
    chooser.setToolTipText(MessageManager.getString("action.load"));

    int value = chooser.showOpenDialog(this);

    if (value == JalviewFileChooser.APPROVE_OPTION)
    {
      File choice = chooser.getSelectedFile();
      jalview.bin.Cache.setProperty("LAST_DIRECTORY", choice.getParent());
      String defaultColours = jalview.bin.Cache.getDefault(
              "USER_DEFINED_COLOURS", choice.getPath());
      if (defaultColours.indexOf(choice.getPath()) == -1)
      {
        defaultColours = defaultColours.concat("|")
                .concat(choice.getPath());
      }

      jalview.bin.Cache.setProperty("USER_DEFINED_COLOURS", defaultColours);

      UserColourScheme ucs = loadColours(choice.getAbsolutePath());
      Color[] colors = ucs.getColours();
      schemeName.setText(ucs.getName());

      if (ucs.getLowerCaseColours() != null)
      {
        caseSensitive.setSelected(true);
        lcaseColour.setEnabled(true);
        resetButtonPanel(true);
        for (int i = 0; i < lowerCaseButtons.size(); i++)
        {
          JButton button = lowerCaseButtons.get(i);
          button.setBackground(ucs.getLowerCaseColours()[i]);
        }

      }
      else
      {
        caseSensitive.setSelected(false);
        lcaseColour.setEnabled(false);
        resetButtonPanel(false);
      }

      for (int i = 0; i < upperCaseButtons.size(); i++)
      {
        JButton button = upperCaseButtons.get(i);
        button.setBackground(colors[i]);
      }

    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public static UserColourScheme loadDefaultColours()
  {
    UserColourScheme ret = null;

    String colours = jalview.bin.Cache.getProperty("USER_DEFINED_COLOURS");
    if (colours != null)
    {
      if (colours.indexOf("|") > -1)
      {
        colours = colours.substring(0, colours.indexOf("|"));
      }

      ret = loadColours(colours);
    }

    if (ret == null)
    {
      Color[] newColours = new Color[24];
      for (int i = 0; i < 24; i++)
      {
        newColours[i] = Color.white;
      }
      ret = new UserColourScheme(newColours);
    }

    return ret;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param file
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  static UserColourScheme loadColours(String file)
  {
    UserColourScheme ucs = null;
    Color[] newColours = null;
    try
    {
      InputStreamReader in = new InputStreamReader(
              new FileInputStream(file), "UTF-8");

      jalview.schemabinding.version2.JalviewUserColours jucs = new jalview.schemabinding.version2.JalviewUserColours();

      org.exolab.castor.xml.Unmarshaller unmar = new org.exolab.castor.xml.Unmarshaller(
              jucs);
      jucs = (jalview.schemabinding.version2.JalviewUserColours) unmar
              .unmarshal(in);

      newColours = new Color[24];

      Color[] lowerCase = null;
      boolean caseSensitive = false;

      String name;
      int index;
      for (int i = 0; i < jucs.getColourCount(); i++)
      {
        name = jucs.getColour(i).getName();
        if (ResidueProperties.aa3Hash.containsKey(name))
        {
          index = ResidueProperties.aa3Hash.get(name).intValue();
        }
        else
        {
          index = ResidueProperties.aaIndex[name.charAt(0)];
        }
        if (index == -1)
        {
          continue;
        }

        if (name.toLowerCase().equals(name))
        {
          if (lowerCase == null)
          {
            lowerCase = new Color[23];
          }
          caseSensitive = true;
          lowerCase[index] = new Color(Integer.parseInt(jucs.getColour(i)
                  .getRGB(), 16));
        }
        else
        {
          newColours[index] = new Color(Integer.parseInt(jucs.getColour(i)
                  .getRGB(), 16));
        }
      }

      if (newColours != null)
      {
        ucs = new UserColourScheme(newColours);
        ucs.setName(jucs.getSchemeName());
        if (caseSensitive)
        {
          ucs.setLowerCaseColours(lowerCase);
        }
      }

    } catch (Exception ex)
    {
      // Could be Archive Jalview format
      try
      {
        InputStreamReader in = new InputStreamReader(new FileInputStream(
                file), "UTF-8");

        jalview.binding.JalviewUserColours jucs = new jalview.binding.JalviewUserColours();

        jucs = jucs.unmarshal(in);

        newColours = new Color[jucs.getColourCount()];

        for (int i = 0; i < 24; i++)
        {
          newColours[i] = new Color(Integer.parseInt(jucs.getColour(i)
                  .getRGB(), 16));
        }
        if (newColours != null)
        {
          ucs = new UserColourScheme(newColours);
          ucs.setName(jucs.getSchemeName());
        }
      } catch (Exception ex2)
      {
        ex2.printStackTrace();
      }

      if (newColours == null)
      {
        System.out.println("Error loading User ColourFile\n" + ex);
      }
    }

    return ucs;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  protected void savebutton_actionPerformed(ActionEvent e)
  {
    if (schemeName.getText().trim().length() < 1)
    {
      JOptionPane.showInternalMessageDialog(Desktop.desktop, MessageManager
              .getString("label.user_colour_scheme_must_have_name"),
              MessageManager.getString("label.no_name_colour_scheme"),
              JOptionPane.WARNING_MESSAGE);
      return;
    }

    if (userColourSchemes != null
            && userColourSchemes.containsKey(schemeName.getText()))
    {
      int reply = JOptionPane.showInternalConfirmDialog(Desktop.desktop,
              MessageManager.formatMessage(
                      "label.colour_scheme_exists_overwrite", new Object[] {
                          schemeName.getText(), schemeName.getText() }),
              MessageManager.getString("label.duplicate_scheme_name"),
              JOptionPane.YES_NO_OPTION);
      if (reply != JOptionPane.YES_OPTION)
      {
        return;
      }

      userColourSchemes.remove(schemeName.getText());
    }
    JalviewFileChooser chooser = new JalviewFileChooser(
            jalview.bin.Cache.getProperty("LAST_DIRECTORY"),
            new String[] { "jc" }, new String[] { "Jalview User Colours" },
            "Jalview User Colours");

    chooser.setFileView(new jalview.io.JalviewFileView());
    chooser.setDialogTitle(MessageManager
            .getString("label.save_colour_scheme"));
    chooser.setToolTipText(MessageManager.getString("action.save"));

    int value = chooser.showSaveDialog(this);

    if (value == JalviewFileChooser.APPROVE_OPTION)
    {
      String choice = chooser.getSelectedFile().getPath();
      String defaultColours = jalview.bin.Cache.getDefault(
              "USER_DEFINED_COLOURS", choice);
      if (defaultColours.indexOf(choice) == -1)
      {
        if (defaultColours.length() > 0)
        {
          defaultColours = defaultColours.concat("|");
        }
        defaultColours = defaultColours.concat(choice);
      }

      userColourSchemes.put(schemeName.getText(), getSchemeFromButtons());

      ap.alignFrame.updateUserColourMenu();

      jalview.bin.Cache.setProperty("USER_DEFINED_COLOURS", defaultColours);

      jalview.schemabinding.version2.JalviewUserColours ucs = new jalview.schemabinding.version2.JalviewUserColours();

      ucs.setSchemeName(schemeName.getText());
      try
      {
        PrintWriter out = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(choice), "UTF-8"));

        for (int i = 0; i < buttonPanel.getComponentCount(); i++)
        {
          JButton button = (JButton) buttonPanel.getComponent(i);
          jalview.schemabinding.version2.Colour col = new jalview.schemabinding.version2.Colour();
          col.setName(button.getText());
          col.setRGB(jalview.util.Format.getHexString(button
                  .getBackground()));
          ucs.addColour(col);
        }

        ucs.marshal(out);
        out.close();
      } catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  protected void cancelButton_actionPerformed(ActionEvent e)
  {
    if (ap != null)
    {
      if (seqGroup != null)
      {
        seqGroup.cs = oldColourScheme;
      }
      else if (ap != null)
      {
        ap.av.setGlobalColourScheme(oldColourScheme);
      }
      ap.paintAlignment(true);
    }

    if (jmol != null)
    {
      jmol.setJalviewColourScheme(oldColourScheme);
    }

    try
    {
      frame.setClosed(true);
    } catch (Exception ex)
    {
    }
  }

  static Hashtable userColourSchemes;

  public static Hashtable getUserColourSchemes()
  {
    return userColourSchemes;
  }

  public static void initUserColourSchemes(String files)
  {
    userColourSchemes = new Hashtable();

    if (files == null || files.length() == 0)
    {
      return;
    }

    // In case colours can't be loaded, we'll remove them
    // from the default list here.
    StringBuffer coloursFound = new StringBuffer();
    StringTokenizer st = new StringTokenizer(files, "|");
    while (st.hasMoreElements())
    {
      String file = st.nextToken();
      try
      {
        UserColourScheme ucs = loadColours(file);
        if (ucs != null)
        {
          if (coloursFound.length() > 0)
          {
            coloursFound.append("|");
          }
          coloursFound.append(file);
          userColourSchemes.put(ucs.getName(), ucs);
        }
      } catch (Exception ex)
      {
        System.out.println("Error loading User ColourFile\n" + ex);
      }
    }
    if (!files.equals(coloursFound.toString()))
    {
      if (coloursFound.toString().length() > 1)
      {
        jalview.bin.Cache.setProperty("USER_DEFINED_COLOURS",
                coloursFound.toString());
      }
      else
      {
        jalview.bin.Cache.applicationProperties
                .remove("USER_DEFINED_COLOURS");
      }
    }
  }

  public static void removeColourFromDefaults(String target)
  {
    // The only way to find colours by name is to load them in
    // In case colours can't be loaded, we'll remove them
    // from the default list here.

    userColourSchemes = new Hashtable();

    StringBuffer coloursFound = new StringBuffer();
    StringTokenizer st = new StringTokenizer(
            jalview.bin.Cache.getProperty("USER_DEFINED_COLOURS"), "|");

    while (st.hasMoreElements())
    {
      String file = st.nextToken();
      try
      {
        UserColourScheme ucs = loadColours(file);
        if (ucs != null && !ucs.getName().equals(target))
        {
          if (coloursFound.length() > 0)
          {
            coloursFound.append("|");
          }
          coloursFound.append(file);
          userColourSchemes.put(ucs.getName(), ucs);
        }
      } catch (Exception ex)
      {
        System.out.println("Error loading User ColourFile\n" + ex);
      }
    }

    if (coloursFound.toString().length() > 1)
    {
      jalview.bin.Cache.setProperty("USER_DEFINED_COLOURS",
              coloursFound.toString());
    }
    else
    {
      jalview.bin.Cache.applicationProperties
              .remove("USER_DEFINED_COLOURS");
    }

  }

  @Override
  public void caseSensitive_actionPerformed(ActionEvent e)
  {
    resetButtonPanel(caseSensitive.isSelected());
    lcaseColour.setEnabled(caseSensitive.isSelected());
  }

  @Override
  public void lcaseColour_actionPerformed(ActionEvent e)
  {
    if (selectedButtons == null)
    {
      selectedButtons = new ArrayList<JButton>();
    }
    else
    {
      selectedButtons.clear();
    }
    selectedButtons.add(lcaseColour);
  }
}
