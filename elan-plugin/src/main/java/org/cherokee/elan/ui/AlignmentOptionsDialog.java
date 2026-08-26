package org.cherokee.elan.ui;

import mpi.eudico.server.corpora.clom.Tier;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AlignmentOptionsDialog extends JDialog {

    private final JComboBox<String> targetTierCombo;
    private final JComboBox<String> scriptTypeCombo;
    private boolean confirmed = false;

    public AlignmentOptionsDialog(Frame parent, List<Tier> availableTargetTiers) {
        super(parent, "Cherokee Forced-Alignment Options", true);
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        formPanel.add(new JLabel("Target Tier:"));
        targetTierCombo = new JComboBox<>();
        for (Tier tier : availableTargetTiers) {
            targetTierCombo.addItem(tier.getName());
        }
        formPanel.add(targetTierCombo);

        formPanel.add(new JLabel("Script Type:"));
        scriptTypeCombo = new JComboBox<>(new String[]{"syllabary", "latin"});
        formPanel.add(scriptTypeCombo);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("Align");
        JButton cancelButton = new JButton("Cancel");

        okButton.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        cancelButton.addActionListener(e -> {
            confirmed = false;
            dispose();
        });

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getSelectedTargetTierName() {
        return (String) targetTierCombo.getSelectedItem();
    }

    public String getSelectedScriptType() {
        return (String) scriptTypeCombo.getSelectedItem();
    }
}
