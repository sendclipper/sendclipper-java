package com.sendclipper.model;

import java.util.List;

/** A domain with the DNS records to publish. */
public class DomainRecords extends ApiObject {
  private Domain domain;
  private List<DnsRecord> records;

  public Domain getDomain() {
    return domain;
  }

  public List<DnsRecord> getRecords() {
    return records;
  }

}
