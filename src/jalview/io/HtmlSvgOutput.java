/*
 * Jalview - A Sequence Alignment Editor and Viewer (Version 2.9)
 * Copyright (C) 2015 The Jalview Authors
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
package jalview.io;

import jalview.api.AlignExportSettingI;
import jalview.api.FeatureRenderer;
import jalview.datamodel.AlignmentExportData;
import jalview.datamodel.SequenceI;
import jalview.gui.AlignViewport;
import jalview.gui.AlignmentPanel;
import jalview.gui.HTMLOptions;
import jalview.math.AlignmentDimension;
import jalview.util.MessageManager;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileOutputStream;

import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.jfree.graphics2d.svg.SVGHints;

public class HtmlSvgOutput
{
  AlignViewport av;

  FeatureRenderer fr;

  AlignmentPanel ap;

  public HtmlSvgOutput(File file, AlignmentPanel ap)
  {
    this.av = ap.av;
    this.ap = ap;
    fr = ap.cloneFeatureRenderer();
    generateHtmlSvgOutput(file);
  }

  public void generateHtmlSvgOutput(File file)
  {
    try
    {
      if (file == null)
      {

        JalviewFileChooser chooser = getHTMLChooser();
        chooser.setFileView(new jalview.io.JalviewFileView());
        chooser.setDialogTitle(ap.alignFrame.getTitle());
        chooser.setToolTipText(MessageManager.getString("action.save"));
        int value = chooser.showSaveDialog(ap.alignFrame);

        if (value == jalview.io.JalviewFileChooser.APPROVE_OPTION)
        {
          jalview.bin.Cache.setProperty("LAST_DIRECTORY", chooser
                  .getSelectedFile().getParent());
          file = chooser.getSelectedFile();
        }
      }

      AlignmentDimension aDimension = ap.getAlignmentDimension();
      SVGGraphics2D g1 = new SVGGraphics2D(aDimension.getWidth(),
              aDimension.getHeight());
      SVGGraphics2D g2 = new SVGGraphics2D(aDimension.getWidth(),
              aDimension.getHeight());

      String renderStyle = jalview.bin.Cache.getDefault("HTML_RENDERING",
              "Prompt each time");

      // If we need to prompt, and if the GUI is visible then
      // Prompt for rendering style
      if (renderStyle.equalsIgnoreCase("Prompt each time")
              && !(System.getProperty("java.awt.headless") != null && System
                      .getProperty("java.awt.headless").equals("true")))
      {
        HTMLOptions svgOption = new HTMLOptions();
        renderStyle = svgOption.getValue();

        if (renderStyle == null || svgOption.cancelled)
        {
          return;
        }
      }

      if (renderStyle.equalsIgnoreCase("lineart"))
      {
        g1.setRenderingHint(SVGHints.KEY_DRAW_STRING_TYPE,
                SVGHints.VALUE_DRAW_STRING_TYPE_VECTOR);
        g2.setRenderingHint(SVGHints.KEY_DRAW_STRING_TYPE,
                SVGHints.VALUE_DRAW_STRING_TYPE_VECTOR);
      }
      printUnwrapped(aDimension.getWidth(), aDimension.getHeight(), 0, g1,
              g2);

      String titleSvgData = g1.getSVGDocument();
      String alignSvgData = g2.getSVGDocument();
      String jsonData = null;
      boolean isEmbbedBioJSON = Boolean.valueOf(jalview.bin.Cache
              .getDefault("EXPORT_EMBBED_BIOJSON", "true"));
      if (isEmbbedBioJSON)
      {
        AlignExportSettingI exportSettings = new AlignExportSettingI()
        {
          @Override
          public boolean isExportHiddenSequences()
          {
            return true;
          }

          @Override
          public boolean isExportHiddenColumns()
          {
            return true;
          }

          @Override
          public boolean isExportAnnotations()
          {
            return true;
          }

          @Override
          public boolean isExportFeatures()
          {
            return true;
          }

          @Override
          public boolean isExportGroups()
          {
            return true;
          }

          @Override
          public boolean isCancelled()
          {
            return false;
          }

        };
        AlignmentExportData exportData = jalview.gui.AlignFrame
                .getAlignmentForExport(JSONFile.FILE_DESC, av,
                        exportSettings);
        jsonData = new FormatAdapter(ap, exportData.getSettings())
                .formatSequences(JSONFile.FILE_DESC,
                        exportData.getAlignment(),
                        exportData.getOmitHidden(),
                        exportData.getStartEndPostions(),
                        av.getColumnSelection());
      }
      String htmlData = getHtml(titleSvgData, alignSvgData, jsonData);
      FileOutputStream out = new FileOutputStream(file);
      out.write(htmlData.getBytes());
      out.flush();
      out.close();
      if (!(System.getProperty("java.awt.headless") != null && System
              .getProperty("java.awt.headless").equals("true")))
      {
        jalview.util.BrowserLauncher.openURL("file:///" + file);
      }
    } catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  static JalviewFileChooser getHTMLChooser()
  {
    return new jalview.io.JalviewFileChooser(
            jalview.bin.Cache.getProperty("LAST_DIRECTORY"),
            new String[] { "html" },
            new String[] { "Hypertext Markup Language" },
            "Hypertext Markup Language");
  }

  public int printUnwrapped(int pwidth, int pheight, int pi, Graphics... pg)
          throws PrinterException
  {
    int idWidth = ap.getVisibleIdWidth(false);
    FontMetrics fm = ap.getFontMetrics(av.getFont());
    int scaleHeight = av.getCharHeight() + fm.getDescent();

    pg[0].setColor(Color.white);
    pg[0].fillRect(0, 0, pwidth, pheight);
    pg[0].setFont(av.getFont());

    // //////////////////////////////////
    // / How many sequences and residues can we fit on a printable page?
    int totalRes = (pwidth - idWidth) / av.getCharWidth();
    int totalSeq = (pheight - scaleHeight) / av.getCharHeight() - 1;
    int pagesWide = (av.getAlignment().getWidth() / totalRes) + 1;

    // ///////////////////////////
    // / Only print these sequences and residues on this page
    int startRes;

    // ///////////////////////////
    // / Only print these sequences and residues on this page
    int endRes;

    // ///////////////////////////
    // / Only print these sequences and residues on this page
    int startSeq;

    // ///////////////////////////
    // / Only print these sequences and residues on this page
    int endSeq;
    startRes = (pi % pagesWide) * totalRes;
    endRes = (startRes + totalRes) - 1;

    if (endRes > (av.getAlignment().getWidth() - 1))
    {
      endRes = av.getAlignment().getWidth() - 1;
    }
    startSeq = (pi / pagesWide) * totalSeq;
    endSeq = startSeq + totalSeq;
    if (endSeq > av.getAlignment().getHeight())
    {
      endSeq = av.getAlignment().getHeight();
    }
    int pagesHigh = ((av.getAlignment().getHeight() / totalSeq) + 1)
            * pheight;
    if (av.isShowAnnotation())
    {
      pagesHigh += ap.getAnnotationPanel().adjustPanelHeight() + 3;
    }
    pagesHigh /= pheight;
    if (pi >= (pagesWide * pagesHigh))
    {
      return Printable.NO_SUCH_PAGE;
    }

    // draw Scale
    pg[1].translate(0, 0);
    ap.getScalePanel().drawScale(pg[1], startRes, endRes, pwidth - idWidth,
            scaleHeight);
    pg[1].translate(-idWidth, scaleHeight);

    // //////////////
    // Draw the ids
    Color currentColor = null;
    Color currentTextColor = null;
    pg[0].translate(0, scaleHeight);
    pg[0].setFont(ap.getIdPanel().getIdCanvas().getIdfont());
    SequenceI seq;
    for (int i = startSeq; i < endSeq; i++)
    {
      seq = av.getAlignment().getSequenceAt(i);
      if ((av.getSelectionGroup() != null)
              && av.getSelectionGroup().getSequences(null).contains(seq))
      {
        currentColor = Color.gray;
        currentTextColor = Color.black;
      }
      else
      {
        currentColor = av.getSequenceColour(seq);
        currentTextColor = Color.black;
      }
      pg[0].setColor(currentColor);
      pg[0].fillRect(0, (i - startSeq) * av.getCharHeight(), idWidth,
              av.getCharHeight());
      pg[0].setColor(currentTextColor);
      int xPos = 0;
      if (av.isRightAlignIds())
      {
        fm = pg[0].getFontMetrics();
        xPos = idWidth
                - fm.stringWidth(seq.getDisplayId(av.getShowJVSuffix()))
                - 4;
      }
      pg[0].drawString(seq.getDisplayId(av.getShowJVSuffix()), xPos,
              (((i - startSeq) * av.getCharHeight()) + av.getCharHeight())
                      - (av.getCharHeight() / 5));
    }
    pg[0].setFont(av.getFont());
    pg[0].translate(idWidth, 0);

    // draw main sequence panel
    pg[1].translate(idWidth, 0);
    ap.getSeqPanel().seqCanvas.drawPanel(pg[1], startRes, endRes, startSeq,
            endSeq, 0);
    if (av.isShowAnnotation() && (endSeq == av.getAlignment().getHeight()))
    {
      // draw annotation label - need to offset for current scroll position
      int offset = -ap.getAlabels().getScrollOffset();
      pg[0].translate(0, offset);
      pg[0].translate(-idWidth - 3,
              (endSeq - startSeq) * av.getCharHeight() + 3);
      ap.getAlabels().drawComponent(pg[0], idWidth);
      pg[0].translate(idWidth + 3, 0);
      pg[0].translate(0, -offset);

      // draw annotation - need to offset for current scroll position
      pg[1].translate(0, offset);
      pg[1].translate(-idWidth - 3,
              (endSeq - startSeq) * av.getCharHeight() + 3);
      pg[1].translate(idWidth + 3, 0);
      ap.getAnnotationPanel().renderer.drawComponent(
              ap.getAnnotationPanel(), av, pg[1], -1, startRes, endRes + 1);
      pg[1].translate(0, -offset);
    }

    return Printable.PAGE_EXISTS;
  }

  private String getHtml(String titleSvg, String alignmentSvg,
          String jsonData)
  {
    StringBuilder htmlSvg = new StringBuilder();
    htmlSvg.append("<html>\n");
    if (jsonData != null)
    {
      htmlSvg.append("<button onclick=\"javascipt:openJalviewUsingCurrentUrl();\">Launch in Jalview</button> &nbsp;");
      htmlSvg.append("<input type=\"submit\" value=\"View raw BioJSON Data\" onclick=\"jQuery.facebox({ div:'#seqData' }); return false;\" />");
      htmlSvg.append("<div style=\"display: none;\" name=\"seqData\" id=\"seqData\" >"
              + jsonData + "</div>");
      htmlSvg.append("<br/>&nbsp;");
    }
    htmlSvg.append("\n<style type=\"text/css\"> "
            + "div.parent{ width:100%;<!-- overflow: auto; -->}\n"
            + "div.titlex{ width:11%; float: left; }\n"
            + "div.align{ width:89%; float: right; }\n"
            + "div.main-container{ border: 2px solid blue; border: 2px solid blue; width: 99%;   min-height: 99%; }\n"
            + ".sub-category-container {overflow-y: scroll; overflow-x: hidden; width: 100%; height: 100%;}\n"
            + "object {pointer-events: none;}");
    if (jsonData != null)
    {
      // facebox style sheet for displaying raw BioJSON data
      htmlSvg.append("#facebox { position: absolute;  top: 0;   left: 0; z-index: 100; text-align: left; }\n"
              + "#facebox .popup{ position:relative; border:3px solid rgba(0,0,0,0); -webkit-border-radius:5px;"
              + "-moz-border-radius:5px; border-radius:5px; -webkit-box-shadow:0 0 18px rgba(0,0,0,0.4); -moz-box-shadow:0 0 18px rgba(0,0,0,0.4);"
              + "box-shadow:0 0 18px rgba(0,0,0,0.4); }\n"
              + "#facebox .content { display:table; width: 98%; padding: 10px; background: #fff; -webkit-border-radius:4px; -moz-border-radius:4px;"
              + " border-radius:4px; }\n"
              + "#facebox .content > p:first-child{ margin-top:0; }\n"
              + "#facebox .content > p:last-child{ margin-bottom:0; }\n"
              + "#facebox .close{ position:absolute; top:5px; right:5px; padding:2px; background:#fff; }\n"
              + "#facebox .close img{ opacity:0.3; }\n"
              + "#facebox .close:hover img{ opacity:1.0; }\n"
              + "#facebox .loading { text-align: center; }\n"
              + "#facebox .image { text-align: center;}\n"
              + "#facebox img { border: 0;  margin: 0; }\n"
              + "#facebox_overlay { position: fixed; top: 0px; left: 0px; height:100%; width:100%; }\n"
              + ".facebox_hide { z-index:-100; }\n"
              + ".facebox_overlayBG { background-color: #000;  z-index: 99;  }");
    }

    htmlSvg.append("</style>");
    htmlSvg.append("<div class=\"main-container\" \n>");
    htmlSvg.append("<div class=\"titlex\">\n");
    htmlSvg.append("<div class=\"sub-category-container\"> \n");
    htmlSvg.append(titleSvg);
    htmlSvg.append("</div>");
    htmlSvg.append("</div>\n\n<!-- ========================================================================================== -->\n\n");
    htmlSvg.append("<div class=\"align\" >");
    htmlSvg.append(
            "<div class=\"sub-category-container\"> <div style=\"overflow-x: scroll;\">")
            .append(alignmentSvg).append("</div></div>").append("</div>");
    htmlSvg.append("</div>");

    htmlSvg.append("<script language=\"JavaScript\" type=\"text/javascript\" src=\"http://ajax.googleapis.com/ajax/libs/jquery/1/jquery.min.js\"></script>\n"
            + "<script language=\"JavaScript\" type=\"text/javascript\"  src=\"http://ajax.googleapis.com/ajax/libs/jqueryui/1.11.2/jquery-ui.min.js\"></script>\n"
            + "<script>\n"
            + "var subCatContainer = $(\".sub-category-container\");\n"
            + "subCatContainer.scroll(\nfunction() {\n"
            + "subCatContainer.scrollTop($(this).scrollTop());\n});\n");

    htmlSvg.append("</script>\n");

    // javascript for launching file in Jalview

    htmlSvg.append("<script language=\"JavaScript\">\n");
    htmlSvg.append("function openJalviewUsingCurrentUrl(){\n");
    htmlSvg.append("    var json = JSON.parse(document.getElementById(\"seqData\").innerHTML);\n");
    htmlSvg.append("    var jalviewVersion = json['appSettings'].version;\n");
    htmlSvg.append("    var url = json['appSettings'].webStartUrl;\n");
    htmlSvg.append("    var myForm = document.createElement(\"form\");\n\n");
    htmlSvg.append("    var heap = document.createElement(\"input\");\n");
    htmlSvg.append("    heap.setAttribute(\"name\", \"jvm-max-heap\") ;\n");
    htmlSvg.append("    heap.setAttribute(\"value\", \"2G\");\n\n");
    htmlSvg.append("    var target = document.createElement(\"input\");\n");
    htmlSvg.append("    target.setAttribute(\"name\", \"open\");\n");
    htmlSvg.append("    target.setAttribute(\"value\", document.URL);\n\n");
    htmlSvg.append("    var jvVersion = document.createElement(\"input\");\n");
    htmlSvg.append("    jvVersion.setAttribute(\"name\", \"version\") ;\n");
    htmlSvg.append("    jvVersion.setAttribute(\"value\", jalviewVersion);\n\n");
    htmlSvg.append("    myForm.action = url;\n");
    htmlSvg.append("    myForm.appendChild(heap);\n");
    htmlSvg.append("    myForm.appendChild(target);\n");
    htmlSvg.append("    myForm.appendChild(jvVersion);\n");
    htmlSvg.append("    document.body.appendChild(myForm);\n");
    htmlSvg.append("    myForm.submit() ;\n");
    htmlSvg.append("    document.body.removeChild(myForm);\n");
    htmlSvg.append("}\n");

    // jquery facebox for displaying raw BioJSON data");
    if (jsonData != null)
    {
      htmlSvg.append("/* Facebox (for jQuery)\n");
      htmlSvg.append("* version: 1.3\n");
      htmlSvg.append(" * @requires jQuery v1.2 or later\n");
      htmlSvg.append(" * @homepage https://github.com/defunkt/facebox\n");
      htmlSvg.append(" * Licensed under the MIT:\n");
      htmlSvg.append(" *   http://www.opensource.org/licenses/mit-license.php\n");
      htmlSvg.append(" * Copyright Forever Chris Wanstrath, Kyle Neath\n");
      htmlSvg.append(" * Usage:\n");
      htmlSvg.append(" *  jQuery(document).ready(function() {\n");
      htmlSvg.append(" *    jQuery('a[rel*=facebox]').facebox()\n");
      htmlSvg.append(" *  })\n");
      htmlSvg.append(" *  <a href=\"#terms\" rel=\"facebox\">Terms</a>\n");
      htmlSvg.append(" *    Loads the #terms div in the box\n");
      htmlSvg.append(" *  <a href=\"terms.html\" rel=\"facebox\">Terms</a>\n");
      htmlSvg.append(" *    Loads the terms.html page in the box\n");
      htmlSvg.append(" *  <a href=\"terms.png\" rel=\"facebox\">Terms</a>\n");
      htmlSvg.append(" *    Loads the terms.png image in the box\n");
      htmlSvg.append(" *  You can also use it programmatically:\n");
      htmlSvg.append(" *    jQuery.facebox('some html')\n");
      htmlSvg.append(" *    jQuery.facebox('some html', 'my-groovy-style')\n");
      htmlSvg.append(" *  The above will open a facebox with \"some html\" as the content.\n");
      htmlSvg.append(" *    jQuery.facebox(function($) {\n");
      htmlSvg.append(" *      $.get('blah.html', function(data) { $.facebox(data) })\n");
      htmlSvg.append(" *    })\n");
      htmlSvg.append(" *  The above will show a loading screen before the passed function is called,\n");
      htmlSvg.append(" *  allowing for a better ajaxy experience.\n");
      htmlSvg.append(" *  The facebox function can also display an ajax page, an image, or the contents of a div:\n");
      htmlSvg.append(" *    jQuery.facebox({ ajax: 'remote.html' })\n");
      htmlSvg.append(" *    jQuery.facebox({ ajax: 'remote.html' }, 'my-groovy-style')\n");
      htmlSvg.append(" *    jQuery.facebox({ image: 'stairs.jpg' })\n");
      htmlSvg.append(" *    jQuery.facebox({ image: 'stairs.jpg' }, 'my-groovy-style')\n");
      htmlSvg.append(" *    jQuery.facebox({ div: '#box' })\n");
      htmlSvg.append(" *    jQuery.facebox({ div: '#box' }, 'my-groovy-style')\n");
      htmlSvg.append(" *    Want to close the facebox?  Trigger the 'close.facebox' document event:\n");
      htmlSvg.append(" *    jQuery(document).trigger('close.facebox')\n");
      htmlSvg.append(" *  Facebox also has a bunch of other hooks:\n");
      htmlSvg.append(" *    loading.facebox\n");
      htmlSvg.append(" *    beforeReveal.facebox\n");
      htmlSvg.append(" *    reveal.facebox (aliased as 'afterReveal.facebox')\n");
      htmlSvg.append(" *    init.facebox\n");
      htmlSvg.append(" *    afterClose.facebox\n");
      htmlSvg.append(" *  Simply bind a function to any of these hooks:\n");
      htmlSvg.append(" *   $(document).bind('reveal.facebox', function() { ...stuff to do after the facebox and contents are revealed... })\n");
      htmlSvg.append(" *\n");
      htmlSvg.append(" */\n");
      htmlSvg.append("(function($) {\n");
      htmlSvg.append("  $.facebox = function(data, klass) {\n");
      htmlSvg.append("    $.facebox.loading()\n");
      htmlSvg.append("    if (data.ajax) fillFaceboxFromAjax(data.ajax, klass)\n");
      htmlSvg.append("    else if (data.image) fillFaceboxFromImage(data.image, klass)\n");
      htmlSvg.append("    else if (data.div) fillFaceboxFromHref(data.div, klass)\n");
      htmlSvg.append("    else if ($.isFunction(data)) data.call($)\n");
      htmlSvg.append("    else $.facebox.reveal(data, klass)\n");
      htmlSvg.append("  }\n");

      htmlSvg.append("  $.extend($.facebox, {\n");
      htmlSvg.append("    settings: {\n");
      htmlSvg.append("      opacity      : 0.2,\n");
      htmlSvg.append("      overlay      : true,\n");
      htmlSvg.append("      loadingImage : 'https://raw.githubusercontent.com/jalview/biojson/gh-pages/images/loading.gif',\n");
      htmlSvg.append("      closeImage   : 'https://raw.githubusercontent.com/jalview/biojson/gh-pages/images/cancel.png',\n");
      htmlSvg.append("      imageTypes   : [ 'png', 'jpg', 'jpeg', 'gif' ],\n");
      htmlSvg.append("      faceboxHtml  : '<div  id=\"facebox\" style=\"display:none; width: 95%; height: 85%; overflow: auto;\"> ");
      htmlSvg.append("      <div class=\"popup\"> ");
      htmlSvg.append("        <div class=\"content\"> ");
      htmlSvg.append("        </div> ");
      htmlSvg.append("        <a href=\"#\" class=\"close\"></a> ");
      htmlSvg.append("      </div> ");
      htmlSvg.append("    </div>'\n");
      htmlSvg.append("    },      \n");
      htmlSvg.append("    loading: function() {\n");
      htmlSvg.append("      init()\n");
      htmlSvg.append("      if ($('#facebox .loading').length == 1) return true\n");
      htmlSvg.append("      showOverlay()      \n");
      htmlSvg.append("      $('#facebox .content').empty().\n");
      htmlSvg.append("        append('<div class=\"loading\"><img src=\"'+$.facebox.settings.loadingImage+'\"/></div>')\n");
      htmlSvg.append("      $('#facebox').show().css({\n");
      htmlSvg.append("        top:    getPageScroll()[1] + (getPageHeight() / 10),\n");
      htmlSvg.append("        left:    $(window).width() / 2 - ($('#facebox .popup').outerWidth() / 2)\n");
      htmlSvg.append("      })      \n");
      htmlSvg.append("      $(document).bind('keydown.facebox', function(e) {\n");
      htmlSvg.append("       if (e.keyCode == 27) $.facebox.close()\n");
      htmlSvg.append("        return true\n");
      htmlSvg.append("      })\n");
      htmlSvg.append("      $(document).trigger('loading.facebox')\n");
      htmlSvg.append("    },\n");
      htmlSvg.append("    reveal: function(data, klass) {\n");
      htmlSvg.append("      $(document).trigger('beforeReveal.facebox')\n");
      htmlSvg.append("      if (klass) $('#facebox .content').addClass(klass)\n");
      htmlSvg.append("      $('#facebox .content').empty().append('<pre><code>'+JSON.stringify(JSON.parse(data),null,4)+'</pre></code>')\n");
      htmlSvg.append("      $('#facebox .popup').children().fadeIn('normal')\n");
      htmlSvg.append("      $('#facebox').css('left', $(window).width() / 2 - ($('#facebox .popup').outerWidth() / 2))\n");
      htmlSvg.append("      $(document).trigger('reveal.facebox').trigger('afterReveal.facebox')\n");
      htmlSvg.append("    },      \n");
      htmlSvg.append("    close: function() {\n");
      htmlSvg.append("      $(document).trigger('close.facebox')\n");
      htmlSvg.append("      return false\n");
      htmlSvg.append("    }\n");
      htmlSvg.append("  })\n");
      htmlSvg.append("  $.fn.facebox = function(settings) {\n");
      htmlSvg.append("    if ($(this).length == 0) return    \n");
      htmlSvg.append("    init(settings)      \n");
      htmlSvg.append("    function clickHandler() {\n");
      htmlSvg.append("      $.facebox.loading(true)      \n");
      htmlSvg.append("      // support for rel=\"facebox.inline_popup\" syntax, to add a class\n");
      htmlSvg.append("      // also supports deprecated \"facebox[.inline_popup]\" syntax\n");
      htmlSvg.append("      var klass = this.rel.match(/facebox\\[?\\.(\\w+)\\]?/)\n");
      htmlSvg.append("      if (klass) klass = klass[1]\n");
      htmlSvg.append("      fillFaceboxFromHref(this.href, klass)\n");
      htmlSvg.append("      return false\n");
      htmlSvg.append("    }      \n");
      htmlSvg.append("    return this.bind('click.facebox', clickHandler)\n");
      htmlSvg.append("  }\n");
      htmlSvg.append("  // called one time to setup facebox on this page\n");
      htmlSvg.append("  function init(settings) {\n");
      htmlSvg.append("    if ($.facebox.settings.inited) return true\n");
      htmlSvg.append("    else $.facebox.settings.inited = true\n");
      htmlSvg.append("    $(document).trigger('init.facebox')\n");
      htmlSvg.append("    makeCompatible()\n");
      htmlSvg.append("    var imageTypes = $.facebox.settings.imageTypes.join('|')\n");
      htmlSvg.append("    $.facebox.settings.imageTypesRegexp = new RegExp('\\\\.(' + imageTypes + ')(\\\\?.*)?$', 'i')\n");

      htmlSvg.append("    if (settings) $.extend($.facebox.settings, settings)\n");
      htmlSvg.append("    $('body').append($.facebox.settings.faceboxHtml)\n");

      htmlSvg.append("    var preload = [ new Image(), new Image() ]\n");
      htmlSvg.append("    preload[0].src = $.facebox.settings.closeImage\n");
      htmlSvg.append("    preload[1].src = $.facebox.settings.loadingImage\n");

      htmlSvg.append("    $('#facebox').find('.b:first, .bl').each(function() {\n");
      htmlSvg.append("      preload.push(new Image())\n");
      htmlSvg.append("      preload.slice(-1).src = $(this).css('background-image').replace(/url\\((.+)\\)/, '$1')\n");
      htmlSvg.append("    })\n");

      htmlSvg.append("    $('#facebox .close')\n");
      htmlSvg.append("      .click($.facebox.close)\n");
      htmlSvg.append("      .append('<img src=\"'\n");
      htmlSvg.append("              + $.facebox.settings.closeImage\n");
      htmlSvg.append("              + '\" class=\"close_image\" title=\"close\">')\n");
      htmlSvg.append("  }\n");

      htmlSvg.append("  // getPageScroll() by quirksmode.com\n");
      htmlSvg.append("  function getPageScroll() {\n");
      htmlSvg.append("    var xScroll, yScroll;\n");
      htmlSvg.append("    if (self.pageYOffset) {\n");
      htmlSvg.append("      yScroll = self.pageYOffset;\n");
      htmlSvg.append("      xScroll = self.pageXOffset;\n");
      htmlSvg.append("    } else if (document.documentElement && document.documentElement.scrollTop) {     // Explorer 6 Strict\n");
      htmlSvg.append("      yScroll = document.documentElement.scrollTop;\n");
      htmlSvg.append("      xScroll = document.documentElement.scrollLeft;\n");
      htmlSvg.append("    } else if (document.body) {// all other Explorers\n");
      htmlSvg.append("      yScroll = document.body.scrollTop;\n");
      htmlSvg.append("      xScroll = document.body.scrollLeft;\n");
      htmlSvg.append("    }\n");
      htmlSvg.append("    return new Array(xScroll,yScroll)\n");
      htmlSvg.append("  }\n");

      // Adapted from getPageSize() by quirksmode.com");
      htmlSvg.append("  function getPageHeight() {\n");
      htmlSvg.append("    var windowHeight\n");
      htmlSvg.append("    if (self.innerHeight) {    // all except Explorer\n");
      htmlSvg.append("      windowHeight = self.innerHeight;\n");
      htmlSvg.append("    } else if (document.documentElement && document.documentElement.clientHeight) { // Explorer 6 Strict Mode\n");
      htmlSvg.append("      windowHeight = document.documentElement.clientHeight;\n");
      htmlSvg.append("    } else if (document.body) { // other Explorers\n");
      htmlSvg.append("      windowHeight = document.body.clientHeight;\n");
      htmlSvg.append("    }\n");
      htmlSvg.append("    return windowHeight\n");
      htmlSvg.append("  }\n");

      htmlSvg.append("  // Backwards compatibility\n");
      htmlSvg.append("  function makeCompatible() {\n");
      htmlSvg.append("    var $s = $.facebox.settings      \n");
      htmlSvg.append("    $s.loadingImage = $s.loading_image || $s.loadingImage\n");
      htmlSvg.append("    $s.closeImage = $s.close_image || $s.closeImage\n");
      htmlSvg.append("    $s.imageTypes = $s.image_types || $s.imageTypes\n");
      htmlSvg.append("    $s.faceboxHtml = $s.facebox_html || $s.faceboxHtml\n");
      htmlSvg.append("  }\n");

      htmlSvg.append("  // Figures out what you want to display and displays it\n");
      htmlSvg.append("  // formats are:\n");
      htmlSvg.append("  //     div: #id\n");
      htmlSvg.append("  //   image: blah.extension\n");
      htmlSvg.append("  //    ajax: anything else\n");
      htmlSvg.append("  function fillFaceboxFromHref(href, klass) {\n");
      htmlSvg.append("    // div\n");
      htmlSvg.append("    if (href.match(/#/)) {\n");
      htmlSvg.append("      var url    = window.location.href.split('#')[0]\n");
      htmlSvg.append("      var target = href.replace(url,'')\n");
      htmlSvg.append("      if (target == '#') return\n");
      htmlSvg.append("      $.facebox.reveal($(target).html(), klass)\n");

      htmlSvg.append("    // image\n");
      htmlSvg.append("    } else if (href.match($.facebox.settings.imageTypesRegexp)) {\n");
      htmlSvg.append("      fillFaceboxFromImage(href, klass)\n");
      htmlSvg.append("    // ajax\n");
      htmlSvg.append("    } else {\n");
      htmlSvg.append("      fillFaceboxFromAjax(href, klass)\n");
      htmlSvg.append("    }\n");
      htmlSvg.append("  }\n");

      htmlSvg.append("  function fillFaceboxFromImage(href, klass) {\n");
      htmlSvg.append("    var image = new Image()\n");
      htmlSvg.append("    image.onload = function() {\n");
      htmlSvg.append("      $.facebox.reveal('<div class=\"image\"><img src=\"' + image.src + '\" /></div>', klass)\n");
      htmlSvg.append("    }\n");
      htmlSvg.append("    image.src = href\n");
      htmlSvg.append("   }\n");

      htmlSvg.append("  function fillFaceboxFromAjax(href, klass) {\n");
      htmlSvg.append("    $.facebox.jqxhr = $.get(href, function(data) { $.facebox.reveal(data, klass) })\n");
      htmlSvg.append("  }\n");

      htmlSvg.append("  function skipOverlay() {\n");
      htmlSvg.append("    return $.facebox.settings.overlay == false || $.facebox.settings.opacity === null\n");
      htmlSvg.append("  }\n");

      htmlSvg.append("  function showOverlay() {\n");
      htmlSvg.append("    if (skipOverlay()) return\n");

      htmlSvg.append("    if ($('#facebox_overlay').length == 0)\n");
      htmlSvg.append("      $(\"body\").append('<div id=\"facebox_overlay\" class=\"facebox_hide\"></div>')\n");

      htmlSvg.append("    $('#facebox_overlay').hide().addClass(\"facebox_overlayBG\")\n");
      htmlSvg.append("      .css('opacity', $.facebox.settings.opacity)\n");
      htmlSvg.append("      .click(function() { $(document).trigger('close.facebox') })\n");
      htmlSvg.append("       .fadeIn(200)\n");
      htmlSvg.append("    return false\n");
      htmlSvg.append("  }\n");

      htmlSvg.append("  function hideOverlay() {\n");
      htmlSvg.append("    if (skipOverlay()) return      \n");
      htmlSvg.append("    $('#facebox_overlay').fadeOut(200, function(){\n");
      htmlSvg.append("      $(\"#facebox_overlay\").removeClass(\"facebox_overlayBG\")\n");
      htmlSvg.append("      $(\"#facebox_overlay\").addClass(\"facebox_hide\")\n");
      htmlSvg.append("      $(\"#facebox_overlay\").remove()\n");
      htmlSvg.append("    })      \n");
      htmlSvg.append("    return false\n");
      htmlSvg.append("  }\n");

      htmlSvg.append("  $(document).bind('close.facebox', function() {\n");
      htmlSvg.append("    if ($.facebox.jqxhr) {\n");
      htmlSvg.append("      $.facebox.jqxhr.abort()\n");
      htmlSvg.append("      $.facebox.jqxhr = null\n");
      htmlSvg.append("    }\n");
      htmlSvg.append("    $(document).unbind('keydown.facebox')\n");
      htmlSvg.append("    $('#facebox').fadeOut(function() {\n");
      htmlSvg.append("      $('#facebox .content').removeClass().addClass('content')\n");
      htmlSvg.append("      $('#facebox .loading').remove()\n");
      htmlSvg.append("      $(document).trigger('afterClose.facebox')\n");
      htmlSvg.append("    })\n");
      htmlSvg.append("    hideOverlay()\n");
      htmlSvg.append("  })\n");

      htmlSvg.append("})(jQuery);\n");

    }

    htmlSvg.append("</script>\n");
    htmlSvg.append("</html>");
    return htmlSvg.toString();
  }
}
