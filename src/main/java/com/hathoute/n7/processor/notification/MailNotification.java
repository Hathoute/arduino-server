package com.hathoute.n7.processor.notification;

import com.hathoute.n7.model.AlertOperation;
import com.hathoute.n7.model.AlertOperationType;
import com.hathoute.n7.utils.DatabaseManager;
import com.hathoute.n7.utils.MailManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class MailNotification implements NotificationType {
  private static final Logger LOGGER = LoggerFactory.getLogger(MailNotification.class);

  private static final String SUBJECT = "Alert Notification for metric %s";
  private static final String BODY = "The value of metric %s (id: %d) is %f (triggered because it is %s %f)";

  @Override
  public void notify(final AlertOperation alertOperation, final float value) {
    LOGGER.debug("Sending mail notification for alert operation {} and value {}", alertOperation, value);
    String metricName;
    try {
      metricName = DatabaseManager.getInstance().getMetricName(alertOperation.metricId());
    } catch (SQLException e) {
      LOGGER.warn("Error while getting metric name for metricId: {}", alertOperation.metricId(), e);
      metricName = "N/A";
    }

    final var subject = String.format(SUBJECT, metricName);
    final var body = String.format(BODY, metricName, alertOperation.metricId(), value,
        operationTypeToString(alertOperation.type()), alertOperation.value());

    MailManager.getInstance().sendMail(subject, body);
  }

  private String operationTypeToString(final AlertOperationType type) {
    return switch (type) {
      case GT -> "greater than";
      case LT -> "less than";
      case EQ -> "equal to";
    };
  }
}
