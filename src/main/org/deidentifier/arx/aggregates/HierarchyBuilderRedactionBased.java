/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.deidentifier.arx.aggregates;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.deidentifier.arx.AttributeType.Hierarchy;

/**
 * This class enables building hierarchies for categorical and non-categorical values
 * using redaction. Data items are 1) aligned left-to-right or right-to-left, 2) differences in
 * length are filled with a padding character, 3) then, equally long values are redacted character by character
 * from left-to-right or right-to-left.
 * 
 * @author Fabian Prasser
 *
 */
public class HierarchyBuilderRedactionBased implements HierarchyBuilder, Serializable {

    private static final long serialVersionUID = 3625654600380531803L;

    public static enum Order {
        RIGHT_TO_LEFT,
        LEFT_TO_RIGHT
    }

    private Order                aligmentOrder      = Order.LEFT_TO_RIGHT;
    private Order                redactionOrder     = Order.RIGHT_TO_LEFT;
    private char                 redactionCharacter = '*';
    private char                 paddingCharacter   = '*';
    private transient String[][] result;

    /**
     * Values are aligned left-to-right and redacted right-to-left. Redacted characters
     * are replaced with the given character. The same character is used for padding.
     * @param redactionCharacter
     */
    public HierarchyBuilderRedactionBased(char redactionCharacter){
        this.redactionCharacter = redactionCharacter;
        this.paddingCharacter = redactionCharacter;
    }
    
    /**
     * Values are aligned according to the alignmentOrder and redacted according to the redactionOrder. 
     * Redacted characters are replaced with the given character. The same character is used for padding.
     * @param alignmentOrder
     * @param redactionOrder
     * @param redactionCharacter
     */
    public HierarchyBuilderRedactionBased(Order alignmentOrder, 
                                          Order redactionOrder, 
                                          char redactionCharacter){
        this.redactionCharacter = redactionCharacter;
        this.paddingCharacter = redactionCharacter;
        this.aligmentOrder = alignmentOrder;
        this.redactionOrder = redactionOrder;
    }
    
    /**
     * Values are aligned according to the alignmentOrder and redacted according to the redactionOrder. 
     * Redacted characters are replaced with the given character. The padding character is used for padding.
     * @param alignmentOrder
     * @param redactionOrder
     * @param paddingCharacter
     * @param redactionCharacter
     */
    public HierarchyBuilderRedactionBased(Order alignmentOrder, 
                                          Order redactionOrder, 
                                          char paddingCharacter, 
                                          char redactionCharacter){
        this.redactionCharacter = redactionCharacter;
        this.paddingCharacter = paddingCharacter;
        this.aligmentOrder = alignmentOrder;
        this.redactionOrder = redactionOrder;
    }
    
    /**
     * Creates a new hierarchy, based on the predefined specification
     * @param data
     * @param type
     * @return
     */
    public Hierarchy create(){
        
        // Check
        if (result == null) {
            throw new IllegalArgumentException("Please call prepare() first");
        }
        
        // Return
        Hierarchy h = Hierarchy.create(result);
        this.result = null;
        return h;
    }

    /**
     * Prepares the builder. Returns a list of the number of equivalence classes per level
     * @return
     */
    public int[] prepare(String[] data){
        
        // Check
        if (this.result == null) {
            prepareResult(data);
        }
        
        // Compute
        int[] sizes = new int[this.result[0].length];
        for (int i=0; i < sizes.length; i++){
            Set<String> set = new HashSet<String>();
            for (int j=0; j<this.result.length; j++) {
                set.add(result[j][i]);
            }
            sizes[i] = set.size();
        }
        
        // Return
        return sizes;
    }
    
    /**
     * Computes the hierarchy
     */
    private void prepareResult(String[] data){

        // Determine length
        int length = Integer.MIN_VALUE;
        for (String s : data) {
            length = Math.max(length, s.length());
        }
        
        // Build padding string
        StringBuilder paddingBuilder = new StringBuilder();
        for (int i=0; i<length; i++) paddingBuilder.append(paddingCharacter);
        String padding = paddingBuilder.toString();

        // Build list of base strings
        String[] base = new String[data.length];
        for (int i=0; i<data.length; i++) {
            if (data[i].length()<length) {
                String pad = padding.substring(0, length - data[i].length());
                if (aligmentOrder == Order.RIGHT_TO_LEFT) {
                    base[i] =  pad + data[i];
                } else {
                    base[i] =  data[i] + pad;
                }
            } else {
                base[i] = data[i];
            }
        }
        
        // Build padding string
        StringBuilder redactionBuilder = new StringBuilder();
        for (int i=0; i<length; i++) redactionBuilder.append(redactionCharacter);
        String redaction = redactionBuilder.toString();

        // Build result
        this.result = new String[base.length][length + 1];
        for (int i=0; i<base.length; i++){
            result[i] = new String[length + 1];
            result[i][0] = data[i];
            for (int j=1; j<length + 1; j++){
                String redact = redaction.substring(0, j);
                if (redactionOrder == Order.RIGHT_TO_LEFT) {
                    result[i][j] =  base[i].substring(0, length - j) + redact;
                } else {
                    result[i][j] =  redact + base[i].substring(0, length - j);
                }
            }
        }
    }
}
