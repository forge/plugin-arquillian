package org.jboss.forge.arquillian.container;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author Paul Bakker - paul.bakker.nl@gmail.com
 */
public class Container implements Comparable<Container>
{

   private static final Map<String, String> ABBREVIATIONS = new HashMap<String, String>();

   static
   {
      ABBREVIATIONS.put("jbossas-", "jboss-as-");
      ABBREVIATIONS.put("wls-", "weblogic-server-");
      ABBREVIATIONS.put("was-", "websphere-as-");
   }

   private String group_id;
   private String artifact_id;
   private String name;
   private ContainerType containerType;
   private List<Dependency> dependencies;
   private Dependency download;
   private List<Configuration> configurations;

   public String getGroup_id()
   {
      return group_id;
   }

   public void setGroup_id(String group_id)
   {
      this.group_id = group_id;
   }

   public String getArtifact_id()
   {
      return artifact_id;
   }

   public void setArtifact_id(String artifact_id)
   {
      this.artifact_id = artifact_id;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public ContainerType getContainerType()
   {
      return containerType;
   }

   public void setContainerType(ContainerType containerType)
   {
      this.containerType = containerType;
   }

   public List<Dependency> getDependencies()
   {
      return dependencies;
   }

   public void setDependencies(List<Dependency> dependencies)
   {
      this.dependencies = dependencies;
   }

   public Dependency getDownload()
   {
      return download;
   }

   public void setDownload(Dependency download)
   {
      this.download = download;
   }

   public List<Configuration> getConfigurations()
   {
      return configurations;
   }

   public void setConfigurations(List<Configuration> configurations)
   {
      this.configurations = configurations;
   }

   public String getId()
   {
      return getBaseId();
   }

   public String getDisplayName()
   {
      return expandAbbr(getBaseId()).replaceAll("-", "_").toUpperCase();
   }

   public String getProfileId()
   {
      return "arquillian-" + getBaseId();
   }

   private String getBaseId()
   {
      String id = getArtifact_id().replaceAll("arquillian-(?:container-)?", "");
      // HACK fix names for JBoss AS containers since they don't follow the naming conventions
      if ("org.jboss.as".equals(getGroup_id()))
      {
         id = id.replace("jboss-as-", "jbossas-") + "-7";
      }

      return id;
   }

   public int compareTo(Container other)
   {
      return getId().compareTo(other.getId());
   }

   @Override
   public String toString()
   {
      return getDisplayName();
   }

   public static String idForDisplayName(String displayName)
   {
      return abbr(displayName.replaceAll("_", "-").toLowerCase());
   }

   public static String expandAbbr(String id)
   {
      for (Map.Entry<String, String> abbr : ABBREVIATIONS.entrySet())
      {
         if (id.contains(abbr.getKey()))
         {
            id = id.replace(abbr.getKey(), abbr.getValue());
         }
      }

      return id;
   }

   public static String abbr(String id)
   {
      for (Map.Entry<String, String> abbr : ABBREVIATIONS.entrySet())
      {
         if (id.contains(abbr.getValue()))
         {
            id = id.replace(abbr.getValue(), abbr.getKey());
         }
      }

      return id;
   }
}
