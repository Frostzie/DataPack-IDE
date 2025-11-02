// Using JavaScript for styling CodeMirror instead of css due to it liking it more... https://codemirror.net/examples/styling/
window.editorThemes = {
  diagnostic: {
    '.cm-tooltip-diagnostic': {
		border: '1px solid black',
		'background-color': 'wheat',
		opacity: '0.9',
		'white-space': 'break-spaces',
		overflow: 'auto',
		height: 'fit-content',
		width: 'fit-content',
		'max-height': '60em',
		'max-width': '60em',
    },
	'.spyglassmc-diagnostic-0': { textDecoration: 'underline 1.5px darkgray' },
	'.spyglassmc-diagnostic-1': { textDecoration: 'underline 1.5px lightblue' },
	'.spyglassmc-diagnostic-2': { textDecoration: 'underline 1.5px orange' },
	'.spyglassmc-diagnostic-3': { textDecoration: 'underline 1.5px red' },
  },
  colorToken: {
  '.spyglassmc-color-token-comment': { color: '#008000' },
	'.spyglassmc-color-token-enum': { color: '#0070C1' },
	'.spyglassmc-color-token-enumMember': { color: '#0070C1' },
	'.spyglassmc-color-token-function': { color: '#795E26' },
	'.spyglassmc-color-token-keyword': { color: '#AF00DB' },
	'.spyglassmc-color-token-modifier': { color: '#001080' },
	'.spyglassmc-color-token-number': { color: '#098658' },
	'.spyglassmc-color-token-operator': { color: '#AF00DB' },
	'.spyglassmc-color-token-property': { color: '#001080' },
	'.spyglassmc-color-token-string': { color: '#A31515' },
	'.spyglassmc-color-token-struct': { color: '#001080' },
	'.spyglassmc-color-token-type': { color: '#267F99' },
	'.spyglassmc-color-token-variable': { color: '#001080' },
	'.spyglassmc-color-token-error': { color: '#FF0000' },
	'.spyglassmc-color-token-literal': { color: '#0000FF' },
	'.spyglassmc-color-token-resourceLocation': { color: '#795E26' },
	'.spyglassmc-color-token-vector': { color: '#098658' },
  }
};