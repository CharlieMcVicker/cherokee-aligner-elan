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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Custom control panel for Cherokee forced-alignment recognizer in ELAN.
 * Integrates ELAN's tier/selection chooser alongside Cherokee-specific settings.
 */
public class CherokeeAlignerPanel extends JPanel implements ParamPreferences {

    private final AbstractSelectionPanel selectionPanel;
    private JComboBox<String> scriptTypeCombo;
    private JTextField targetTierField;

    public CherokeeAlignerPanel(AbstractSelectionPanel selectionPanel) {
        this.selectionPanel = selectionPanel;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Selection panel at top/center if provided by ELAN host
        if (selectionPanel != null) {
            selectionPanel.setBorder(new CompoundBorder(
                    new TitledBorder("Input Tier / Selection"),
                    new EmptyBorder(5, 5, 5, 5)
            ));
            add(selectionPanel, BorderLayout.CENTER);
        }

        // Settings panel
        JPanel settingsPanel = new JPanel(new GridBagLayout());
        settingsPanel.setBorder(new CompoundBorder(
                new TitledBorder("Alignment Settings"),
                new EmptyBorder(8, 8, 8, 8)
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

        // Target words tier
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        settingsPanel.add(new JLabel("Target Words Tier:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        targetTierField = new JTextField("words", 15);
        settingsPanel.add(targetTierField, gbc);

        add(settingsPanel, BorderLayout.SOUTH);
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
        if (targetTierField != null && !targetTierField.getText().trim().isEmpty()) {
            return targetTierField.getText().trim();
        }
        return "words";
    }

    public void setTargetTierName(String tierName) {
        if (targetTierField != null && tierName != null) {
            targetTierField.setText(tierName);
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

    public void validateParameters() throws RecognizerConfigurationException {
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
