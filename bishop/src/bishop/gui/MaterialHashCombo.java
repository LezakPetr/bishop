package bishop.gui;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JComboBox;

import bishop.base.Color;
import bishop.base.IMaterialHashRead;
import bishop.base.MaterialHash;

@SuppressWarnings("serial")
public class MaterialHashCombo extends JComboBox<String> {
	
	private IMaterialHashRead[] materialHashes;

	public IMaterialHashRead getSelectedMaterialHash() {
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
	
	public void initialize (final Set<IMaterialHashRead> materialHashSet) {
		final List<IMaterialHashRead> materialHashList = new LinkedList<>();
		
		for (IMaterialHashRead hash: materialHashSet) {
			if (hash.getOnTurn() == Color.WHITE) {
				materialHashList.add(hash);
			}
		}
		
		materialHashes = materialHashList.toArray(IMaterialHashRead.EMPTY_ARRAY);
		Arrays.sort(materialHashes);
		
		this.removeAllItems();
		
		for (IMaterialHashRead hash: materialHashes) {
			this.addItem(hash.getMaterialString());
		}
	}
}
