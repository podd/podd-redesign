/**
 * 
 */
package com.github.podd.api.file;

/**
 * Encapsulates SSH File References that are tracked inside of PODD Artifacts.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public interface PoddSSHFileReference extends PoddFileReference
{
    
    /**
     * @return The "filename" component which is needed to identify and locate this SSH file
     *         reference.
     */
    String getFilename();
    
    /**
     * @return The "path" component which is needed to identify and locate this SSH file reference.
     */
    String getPath();
    
    /**
     * @param filename
     *            The "filename" component which is needed to identify and locate this SSH file
     *            reference.
     */
    void setFilename(final String filename);
    
    /**
     * @param path
     *            The "path" component which is needed to identify and locate this SSH file
     *            reference.
     */
    void setPath(final String path);
}
