package com.bftcom.devcomp.api;

/**
 * ���������
 */
public interface IBotConst {
  // �������� ��������
  String QUEUE_TO_SERVICE_PREFIX = "TO_SERVICE_QUEUE_";     // �������, ������� ������� ������
  String QUEUE_TO_ADAPTER_PREFIX = "TO_ADAPTER_QUEUE_";     // �������, ������� ������� ��������
  String QUEUE_TO_BOT_PREFIX = "TO_BOT_QUEUE_";       // �������, ������� ������� ����
  String QUEUE_FROM_BOT_PREFIX = "FROM_BOT_QUEUE_";   // �������, ������� ������� ������ (��� ������� ���� ����������� �������)

  // ����� ��������� ���������, ������������ � ���������
  String PROP_ADAPTER_NAME = "ADAPTER_NAME";                // ��������, ������������ ��� ��������
  String PROP_BOT_NAME = "BOT_NAME";                        // ��������, ������������ ��� ���������� ��������
  String PROP_USER_NAME = "USER_NAME";                      // ��������, ������������ ��� ������������

  // ����� ��������������� ���������, ������������ � ���������
  String PROP_BODY_TEXT = "BODY_TEXT";                     // ��������, ������������ �����, ������������ �� ���� � ������� (����� ���������)

}
