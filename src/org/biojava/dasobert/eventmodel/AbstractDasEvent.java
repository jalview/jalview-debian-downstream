/*
 *                  BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 * 
 * Created on Nov 17, 2006
 * 
 */

package org.biojava.dasobert.eventmodel;

import org.biojava.dasobert.dasregistry.Das1Source;

public class AbstractDasEvent
{

  Das1Source dasSource;

  public Das1Source getSource()
  {
    return dasSource;
  }

  public void setSource(Das1Source source)
  {
    this.dasSource = source;
  }

}
