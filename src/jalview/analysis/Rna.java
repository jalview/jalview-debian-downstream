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
/* Author: Lauren Michelle Lui 
 * Methods are based on RALEE methods http://personalpages.manchester.ac.uk/staff/sam.griffiths-jones/software/ralee/
 * Additional Author: Jan Engelhart (2011) - Structure consensus and bug fixing
 * Additional Author: Anne Menard (2012) - Pseudoknot support and secondary structure consensus
 * */

package jalview.analysis;

import jalview.analysis.SecStrConsensus.SimpleBP;
import jalview.datamodel.SequenceFeature;
import jalview.util.MessageManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

public class Rna
{

  static Hashtable<Integer, Integer> pairHash = new Hashtable();

  private static final Character[] openingPars = { '(', '[', '{', '<', 'A',
      'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
      'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };

  private static final Character[] closingPars = { ')', ']', '}', '>', 'a',
      'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
      'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

  private static HashSet<Character> openingParsSet = new HashSet<Character>(
          Arrays.asList(openingPars));

  private static HashSet<Character> closingParsSet = new HashSet<Character>(
          Arrays.asList(closingPars));

  private static Hashtable<Character, Character> closingToOpening = new Hashtable<Character, Character>()
  // Initializing final data structure
  {
    private static final long serialVersionUID = 1L;
    {
      for (int i = 0; i < openingPars.length; i++)
      {
        // System.out.println(closingPars[i] + "->" + openingPars[i]);
        put(closingPars[i], openingPars[i]);
      }
    }
  };

  private static boolean isOpeningParenthesis(char c)
  {
    return openingParsSet.contains(c);
  }

  private static boolean isClosingParenthesis(char c)
  {
    return closingParsSet.contains(c);
  }

  private static char matchingOpeningParenthesis(char closingParenthesis)
          throws WUSSParseException
  {
    if (!isClosingParenthesis(closingParenthesis))
    {
      throw new WUSSParseException(
              MessageManager.formatMessage(
                      "exception.querying_matching_opening_parenthesis_for_non_closing_parenthesis",
                      new String[] { new StringBuffer(closingParenthesis)
                              .toString() }), -1);
    }

    return closingToOpening.get(closingParenthesis);
  }

  /**
   * Based off of RALEE code ralee-get-base-pairs. Keeps track of open bracket
   * positions in "stack" vector. When a close bracket is reached, pair this
   * with the last element in the "stack" vector and store in "pairs" vector.
   * Remove last element in the "stack" vector. Continue in this manner until
   * the whole string is processed.
   * 
   * @param line
   *          Secondary structure line of an RNA Stockholm file
   * @return Array of SequenceFeature; type = RNA helix, begin is open base
   *         pair, end is close base pair
   */
  public static Vector<SimpleBP> GetSimpleBPs(CharSequence line)
          throws WUSSParseException
  {
    Hashtable<Character, Stack<Integer>> stacks = new Hashtable<Character, Stack<Integer>>();
    Vector<SimpleBP> pairs = new Vector<SimpleBP>();
    int i = 0;
    while (i < line.length())
    {
      char base = line.charAt(i);

      if (isOpeningParenthesis(base))
      {
        if (!stacks.containsKey(base))
        {
          stacks.put(base, new Stack<Integer>());
        }
        stacks.get(base).push(i);

      }
      else if (isClosingParenthesis(base))
      {

        char opening = matchingOpeningParenthesis(base);

        if (!stacks.containsKey(opening))
        {
          throw new WUSSParseException(MessageManager.formatMessage(
                  "exception.mismatched_unseen_closing_char",
                  new String[] { new StringBuffer(base).toString() }), i);
        }

        Stack<Integer> stack = stacks.get(opening);
        if (stack.isEmpty())
        {
          // error whilst parsing i'th position. pass back
          throw new WUSSParseException(MessageManager.formatMessage(
                  "exception.mismatched_closing_char",
                  new String[] { new StringBuffer(base).toString() }), i);
        }
        int temp = stack.pop();

        pairs.add(new SimpleBP(temp, i));
      }
      i++;
    }
    for (char opening : stacks.keySet())
    {
      Stack<Integer> stack = stacks.get(opening);
      if (!stack.empty())
      {
        throw new WUSSParseException(MessageManager.formatMessage(
                "exception.mismatched_opening_char",
                new String[] { new StringBuffer(opening).toString(),
                    Integer.valueOf(stack.pop()).toString() }), i);
      }
    }
    return pairs;
  }

  public static SequenceFeature[] GetBasePairs(CharSequence line)
          throws WUSSParseException
  {
    Vector<SimpleBP> bps = GetSimpleBPs(line);
    SequenceFeature[] outPairs = new SequenceFeature[bps.size()];
    for (int p = 0; p < bps.size(); p++)
    {
      SimpleBP bp = bps.elementAt(p);
      outPairs[p] = new SequenceFeature("RNA helix", "", "", bp.getBP5(),
              bp.getBP3(), "");
    }
    return outPairs;
  }

  public static ArrayList<SimpleBP> GetModeleBP(CharSequence line)
          throws WUSSParseException
  {
    Vector<SimpleBP> bps = GetSimpleBPs(line);
    return new ArrayList<SimpleBP>(bps);
  }

  /**
   * Function to get the end position corresponding to a given start position
   * 
   * @param indice
   *          - start position of a base pair
   * @return - end position of a base pair
   */
  /*
   * makes no sense at the moment :( public int findEnd(int indice){ //TODO:
   * Probably extend this to find the start to a given end? //could be done by
   * putting everything twice to the hash ArrayList<Integer> pair = new
   * ArrayList<Integer>(); return pairHash.get(indice); }
   */

  /**
   * Figures out which helix each position belongs to and stores the helix
   * number in the 'featureGroup' member of a SequenceFeature Based off of RALEE
   * code ralee-helix-map.
   * 
   * @param pairs
   *          Array of SequenceFeature (output from Rna.GetBasePairs)
   */
  public static void HelixMap(SequenceFeature[] pairs)
  {

    int helix = 0; // Number of helices/current helix
    int lastopen = 0; // Position of last open bracket reviewed
    int lastclose = 9999999; // Position of last close bracket reviewed
    int i = pairs.length; // Number of pairs

    int open; // Position of an open bracket under review
    int close; // Position of a close bracket under review
    int j; // Counter

    Hashtable helices = new Hashtable(); // Keep track of helix number for each
                                         // position

    // Go through each base pair and assign positions a helix
    for (i = 0; i < pairs.length; i++)
    {

      open = pairs[i].getBegin();
      close = pairs[i].getEnd();

      // System.out.println("open " + open + " close " + close);
      // System.out.println("lastclose " + lastclose + " lastopen " + lastopen);

      // we're moving from right to left based on closing pair
      /*
       * catch things like <<..>>..<<..>> |
       */
      if (open > lastclose)
      {
        helix++;
      }

      /*
       * catch things like <<..<<..>>..<<..>>>> |
       */
      j = pairs.length - 1;
      while (j >= 0)
      {
        int popen = pairs[j].getBegin();

        // System.out.println("j " + j + " popen " + popen + " lastopen "
        // +lastopen + " open " + open);
        if ((popen < lastopen) && (popen > open))
        {
          if (helices.containsValue(popen)
                  && (((Integer) helices.get(popen)) == helix))
          {
            continue;
          }
          else
          {
            helix++;
            break;
          }
        }

        j -= 1;
      }

      // Put positions and helix information into the hashtable
      helices.put(open, helix);
      helices.put(close, helix);

      // Record helix as featuregroup
      pairs[i].setFeatureGroup(Integer.toString(helix));

      lastopen = open;
      lastclose = close;

    }
  }
}
