package com.lilithbloom.web;

import com.lilithbloom.Workshops;
import java.util.List;
import javax.faces.bean.*;

@ManagedBean(name = "workshops")
@ApplicationScoped
public class WorkshopBean {
  public List<String> getOptions() {
    return Workshops.OPTIONS;
  }

  public List<Workshops.Schedule> getArchive() {
    return Workshops.archive();
  }
}
