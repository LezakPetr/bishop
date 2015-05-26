package bishop.gui;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JComboBox;

import bishop.base.Color;
import bishop.base.MaterialHash;

@SuppressWarnings("serial")
public class MaterialHashCombo extends JComboBox<String> {
	
	private MaterialHash[] materialHashes;

	public MaterialHash getSelectedMaterialHash() {
		final int index = getSelectedIndex();
		
		return materialHashes[index];
	}
	
	public void setSelectedMaterialHash(final MaterialHash hash) {
		for (int i = 0; i < materialHashes.length; i++) {
			if (materialHashes[i].equals(hash)) {
				setSelectedIndex(i);
				return;
			}
		}
	}
	
	public void initialize (final Set<MaterialHash> materialHashSet) {
		final List<MaterialHash> materialHashList = new LinkedList<MaterialHash>();
		
		for (MaterialHash hash: materialHashSet) {
			if (hash.getOnTurn() == Color.WHITE) {
				materialHashList.add(hash);
			}
		}
		
		materialHashes = materialHashList.toArray(new MaterialHash[materialHashList.size()]);
		Arrays.sort(materialHashes);
		
		this.removeAllItems();
		
		for (MaterialHash hash: materialHashes) {
			this.addItem(hash.getMaterialString());
		}
	}
}
