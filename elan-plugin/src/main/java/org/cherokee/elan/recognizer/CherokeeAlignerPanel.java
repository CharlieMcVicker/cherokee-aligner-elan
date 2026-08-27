package org.cherokee.elan.recognizer;

import mpi.eudico.client.annotator.recognizer.api.AbstractSelectionPanel;
import mpi.eudico.client.annotator.recognizer.api.ParamPreferences;
import mpi.eudico.client.annotator.recognizer.api.RecognizerConfigurationException;
import mpi.eudico.client.annotator.recognizer.data.RSelection;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Custom control panel for Cherokee forced-alignment recognizer in ELAN.
 * Integrates Connection Settings, ELAN's tier/selection chooser, and Cherokee alignment settings.
 */
public class CherokeeAlignerPanel extends JPanel implements ParamPreferences {

    private static final String DEFAULT_SERVER_URL = "http://localhost:5050";
    private static final String AUTO_CREATE_WORDS = "[Auto-create: words]";

    private final AbstractSelectionPanel selectionPanel;
    private JTextField serverUrlField;
    private JComboBox<String> scriptTypeCombo;
    private JComboBox<String> targetTierCombo;

    public CherokeeAlignerPanel(AbstractSelectionPanel selectionPanel) {
        this(selectionPanel, null);
    }

    public CherokeeAlignerPanel(AbstractSelectionPanel selectionPanel, List<String> availableTiers) {
        this.selectionPanel = selectionPanel;
        initComponents(availableTiers);
    }

    private void initComponents(List<String> availableTiers) {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // 1. Connection Settings Panel (Top)
        JPanel connectionPanel = new JPanel(new GridBagLayout());
        connectionPanel.setBorder(new CompoundBorder(
                new TitledBorder("Connection Settings"),
                new EmptyBorder(6, 8, 6, 8)
        ));

        GridBagConstraints gbcConn = new GridBagConstraints();
        gbcConn.insets = new Insets(4, 4, 4, 4);
        gbcConn.anchor = GridBagConstraints.WEST;
        gbcConn.fill = GridBagConstraints.HORIZONTAL;

        gbcConn.gridx = 0;
        gbcConn.gridy = 0;
        gbcConn.weightx = 0.0;
        connectionPanel.add(new JLabel("Server URL:"), gbcConn);

        gbcConn.gridx = 1;
        gbcConn.weightx = 1.0;
        serverUrlField = new JTextField(DEFAULT_SERVER_URL, 25);
        connectionPanel.add(serverUrlField, gbcConn);

        add(connectionPanel, BorderLayout.NORTH);

        // 2. Input Tier / Selection Panel (Center)
        if (selectionPanel != null) {
            selectionPanel.setBorder(new CompoundBorder(
                    new TitledBorder("Input Tier / Selection"),
                    new EmptyBorder(5, 5, 5, 5)
            ));
            add(selectionPanel, BorderLayout.CENTER);
        }

        // 3. Alignment Settings Panel (Bottom)
        JPanel settingsPanel = new JPanel(new GridBagLayout());
        settingsPanel.setBorder(new CompoundBorder(
                new TitledBorder("Alignment Settings"),
                new EmptyBorder(6, 8, 6, 8)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Script type
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        settingsPanel.add(new JLabel("Script Type:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        scriptTypeCombo = new JComboBox<>(new String[]{"syllabary", "latin"});
        scriptTypeCombo.setSelectedItem("syllabary");
        settingsPanel.add(scriptTypeCombo, gbc);

        // Target words tier dropdown
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        settingsPanel.add(new JLabel("Target Words Tier:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        targetTierCombo = new JComboBox<>();
        updateAvailableTiers(availableTiers);
        settingsPanel.add(targetTierCombo, gbc);

        add(settingsPanel, BorderLayout.SOUTH);
    }

    public String getServerUrl() {
        if (serverUrlField != null) {
            return serverUrlField.getText().trim();
        }
        return DEFAULT_SERVER_URL;
    }

    public void setServerUrl(String serverUrl) {
        if (serverUrlField != null && serverUrl != null) {
            serverUrlField.setText(serverUrl.trim());
        }
    }

    public String getScriptType() {
        return (scriptTypeCombo != null && scriptTypeCombo.getSelectedItem() != null)
                ? (String) scriptTypeCombo.getSelectedItem()
                : "syllabary";
    }

    public void setScriptType(String scriptType) {
        if (scriptTypeCombo != null && scriptType != null) {
            scriptTypeCombo.setSelectedItem(scriptType);
        }
    }

    public String getTargetTierName() {
        if (targetTierCombo != null && targetTierCombo.getSelectedItem() != null) {
            String selected = ((String) targetTierCombo.getSelectedItem()).trim();
            if (selected.startsWith("[") && selected.endsWith("]")) {
                int colonIdx = selected.indexOf(':');
                if (colonIdx != -1) {
                    return selected.substring(colonIdx + 1, selected.length() - 1).trim();
                }
                return "words";
            }
            return selected;
        }
        return "words";
    }

    public boolean isAutoCreateTargetTier() {
        if (targetTierCombo != null && targetTierCombo.getSelectedItem() != null) {
            String selected = ((String) targetTierCombo.getSelectedItem()).trim();
            return selected.startsWith("[") && selected.endsWith("]");
        }
        return false;
    }

    public void setTargetTierName(String tierName) {
        if (targetTierCombo != null && tierName != null) {
            String cleanName = tierName.trim();
            for (int i = 0; i < targetTierCombo.getItemCount(); i++) {
                String item = targetTierCombo.getItemAt(i);
                if (item.equalsIgnoreCase(cleanName)) {
                    targetTierCombo.setSelectedIndex(i);
                    return;
                }
                if (item.startsWith("[") && item.toLowerCase().contains(cleanName.toLowerCase())) {
                    targetTierCombo.setSelectedIndex(i);
                    return;
                }
            }
            // If custom tier name not found in list, append it and select
            targetTierCombo.addItem(cleanName);
            targetTierCombo.setSelectedItem(cleanName);
        }
    }

    public void updateAvailableTiers(List<String> tierNames) {
        if (targetTierCombo == null) {
            return;
        }
        String currentSelection = (targetTierCombo.getSelectedItem() != null)
                ? (String) targetTierCombo.getSelectedItem()
                : null;

        targetTierCombo.removeAllItems();
        targetTierCombo.addItem(AUTO_CREATE_WORDS);

        if (tierNames != null) {
            for (String tier : tierNames) {
                if (tier != null && !tier.trim().isEmpty() && !tier.startsWith("[")) {
                    String clean = tier.trim();
                    boolean exists = false;
                    for (int i = 0; i < targetTierCombo.getItemCount(); i++) {
                        if (clean.equalsIgnoreCase(targetTierCombo.getItemAt(i))) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        targetTierCombo.addItem(clean);
                    }
                }
            }
        }

        if (currentSelection != null) {
            setTargetTierName(currentSelection);
        } else {
            targetTierCombo.setSelectedIndex(0);
        }
    }

    public AbstractSelectionPanel getSelectionPanel() {
        return selectionPanel;
    }

    @SuppressWarnings("unchecked")
    public List<RSelection> getSelections() {
        if (selectionPanel != null) {
            Object val = selectionPanel.getSelectionValue();
            if (val instanceof List<?>) {
                return (List<RSelection>) val;
            }
        }
        return new ArrayList<>();
    }

    public void validateServerUrl() throws RecognizerConfigurationException {
        String url = getServerUrl();
        if (url == null || url.trim().isEmpty()) {
            throw new RecognizerConfigurationException("Server URL cannot be empty.");
        }
        try {
            URI uri = new URI(url.trim());
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new RecognizerConfigurationException("Server URL must start with http:// or https:// (found: " + url + ")");
            }
            if (uri.getHost() == null || uri.getHost().trim().isEmpty()) {
                throw new RecognizerConfigurationException("Server URL must contain a valid host (found: " + url + ")");
            }
            if (uri.getPort() != -1 && (uri.getPort() < 1 || uri.getPort() > 65535)) {
                throw new RecognizerConfigurationException("Server URL has an invalid port: " + uri.getPort());
            }
        } catch (RecognizerConfigurationException rce) {
            throw rce;
        } catch (Exception e) {
            throw new RecognizerConfigurationException("Invalid Server URL format: " + url);
        }
    }

    public void validateParameters() throws RecognizerConfigurationException {
        validateServerUrl();
        if (selectionPanel != null) {
            Object val = selectionPanel.getSelectionValue();
            Map<String, Object> paramMap = selectionPanel.getParamValue();
            if (val == null && (paramMap == null || paramMap.isEmpty())) {
                throw new RecognizerConfigurationException("Please select a source tier or annotation selection.");
            }
        }
    }

    public void updateLocale(Locale locale) {
        if (selectionPanel != null) {
            selectionPanel.updateLocale(locale);
        }
    }

    public void updateLocaleBundle(ResourceBundle bundle) {
        if (selectionPanel != null) {
            selectionPanel.updateLocaleBundle(bundle);
        }
    }

    @Override
    public Map<String, Object> getParamPreferences() {
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("server_url", getServerUrl());
        prefs.put("script_type", getScriptType());
        prefs.put("target_tier", getTargetTierName());
        if (selectionPanel != null) {
            selectionPanel.getStorableParamPreferencesMap(prefs);
        }
        return prefs;
    }

    @Override
    public void setParamPreferences(Map<String, Object> map) {
        if (map == null) {
            return;
        }
        if (map.containsKey("server_url")) {
            setServerUrl((String) map.get("server_url"));
        }
        if (map.containsKey("script_type")) {
            setScriptType((String) map.get("script_type"));
        }
        if (map.containsKey("target_tier")) {
            setTargetTierName((String) map.get("target_tier"));
        }
        if (selectionPanel != null) {
            selectionPanel.setParamValue(map);
        }
    }
}
