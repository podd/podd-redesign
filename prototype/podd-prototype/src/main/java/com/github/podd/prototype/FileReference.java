package com.github.podd.prototype;

/**
 * An abstract class to represent "File Reference" objects that PODD has to maintain.
 * 
 * @author kutila
 * @created 2012/11/07
 */
public abstract class FileReference
{
    
    private String artifactUri;
    private String objectUri;
    
    private String serverAlias;
    
    /**
     * @return True if this FileReference has its mandatory attributes filled, False otherwise.
     */
    public abstract boolean isFilled();
    
    public String getArtifactUri()
    {
        return this.artifactUri;
    }
    
    public void setArtifactUri(final String artifactUri)
    {
        this.artifactUri = artifactUri;
    }
    
    public String getObjectUri()
    {
        return this.objectUri;
    }
    
    public void setObjectUri(final String objectUri)
    {
        this.objectUri = objectUri;
    }
    
    public String getServerAlias()
    {
        return this.serverAlias;
    }
    
    public void setServerAlias(final String serverAlias)
    {
        this.serverAlias = serverAlias;
    }
    
    @Override
    public String toString()
    {
        final StringBuilder b = new StringBuilder();
        b.append(super.toString());
        b.append(" artifactUri=");
        b.append(this.artifactUri);
        b.append(" objectUri=");
        b.append(this.objectUri);
        b.append(" serverAlias=");
        b.append(this.serverAlias);
        return b.toString();
    }
}
