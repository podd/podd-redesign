package com.github.podd.prototype;

/**
 * A class to reference "File Reference" objects that PODD has to maintain.
 * 
 * @author kutila
 * @created 2012/11/07
 */
public class FileReference
{

    private String artifactUri;
    private String objectUri;
    
    private String serverAlias;

    public String getArtifactUri()
    {
        return artifactUri;
    }

    public void setArtifactUri(String artifactUri)
    {
        this.artifactUri = artifactUri;
    }

    public String getObjectUri()
    {
        return objectUri;
    }

    public void setObjectUri(String objectUri)
    {
        this.objectUri = objectUri;
    }

    public String getServerAlias()
    {
        return serverAlias;
    }

    public void setServerAlias(String serverAlias)
    {
        this.serverAlias = serverAlias;
    }
    
    @Override
    public String toString()
    {
        StringBuilder b = new StringBuilder();
        b.append(super.toString());
        b.append(" artifactUri=");
        b.append(artifactUri);
        b.append(" objectUri=");
        b.append(objectUri);
        b.append(" serverAlias=");
        b.append(serverAlias);
        return b.toString();
    }
}
