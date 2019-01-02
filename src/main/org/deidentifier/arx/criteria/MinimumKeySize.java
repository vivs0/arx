/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.deidentifier.arx.criteria;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.certificate.elements.ElementData;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyArray;
import org.deidentifier.arx.risk.msu.SUDA2;
import org.deidentifier.arx.risk.msu.SUDA2Listener;

/**
 * This criterion ensures that all keys are at least of a given size
 * 
 * @author Fabian Prasser
 */
public class MinimumKeySize extends MatrixBasedCriterion {

    /** SVUID */
    private static final long serialVersionUID = 4317638260873614586L;

    /** Parameter */
    private final int         minKeySize;

    /**
     * Creates a new instance of this criterion.
     * 
     * @param minKeySize
     */
    public MinimumKeySize(int minKeySize) {
        super(false, true);
        this.minKeySize = minKeySize;
    }

    @Override
    public MinimumKeySize clone() {
        return new MinimumKeySize(this.minKeySize);
    }

    @Override
    public void enforce(final HashGroupifyArray array, int numMaxSuppressedOutliers) {
        new SUDA2(array.getArray().getArray()).findKeys(minKeySize - 1, new SUDA2Listener() {
            @Override
            public void msuFound(int row, int size) {
                array.suppress(row);
            }
        });
    }
    
    /**
     * Returns the risk threshold
     * @return
     */
    public int getMinimumKeySize() {
        return this.minKeySize;
    }

    @Override
    public int getRequirements() {
        // Requires only one counter
        return ARXConfiguration.REQUIREMENT_COUNTER;
    }

    @Override
    public boolean isLocalRecodingSupported() {
        return true;
    }

    @Override
    public ElementData render() {
        ElementData result = new ElementData("Minimum key size");
        result.addProperty("Threshold", minKeySize);
        return result;
    }

    @Override
    public String toString() {
        return "(" + minKeySize + ")-minimum-key-size";
    }
}